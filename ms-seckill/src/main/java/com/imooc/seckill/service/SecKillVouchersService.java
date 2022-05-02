package com.imooc.seckill.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.constants.RedisKeyConstant;
import com.imooc.commons.exception.ParamterException;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.SeckillVouchers;
import com.imooc.commons.model.pojo.VoucherOrders;
import com.imooc.commons.model.vo.SignInDinerInfo;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.seckill.mapper.SecKillVouchersMapper;
import com.imooc.seckill.mapper.VoucherOrdersMapper;
import com.imooc.seckill.model.RedisLock;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-15 12:11
 **/
@Service
public class SecKillVouchersService {


  @Resource
  private SecKillVouchersMapper secKillVouchersMapper;

  @Value("${service.name.ms-oauth-server}")
  private String oauthServerName;

  @Resource
  private VoucherOrdersMapper voucherOrdersMapper;

  @Resource
  private RestTemplate restTemplate;

  @Resource
  private RedisTemplate redisTemplate;

  @Resource
  private DefaultRedisScript redisScript;

  @Resource
  private RedisLock redisLock;    // 使用redis的分布式锁 限制一人一单


  @Transactional(rollbackFor = Exception.class)
  public void addVourchers(SeckillVouchers seckillVouchers) {
    // 非空校验
    AssertUtil.isTrue(seckillVouchers.getFkVoucherId() == null, "请选择需要抢购的代金券");
    AssertUtil.isTrue(seckillVouchers.getAmount() == 0, "请输入抢购总数量");

    Date now = new Date();
    AssertUtil.isNotNull(seckillVouchers.getStartTime(), "请输入开始时间");
    AssertUtil.isNotNull(seckillVouchers.getEndTime(), "请输入结束时间");

    // 结束时间不能早于当前时间
    AssertUtil.isTrue(now.after(seckillVouchers.getEndTime()), "结束时间不能早于当前时间");
    // 结束时间不能早于开始时间
    AssertUtil.isTrue(seckillVouchers.getStartTime().after(seckillVouchers.getEndTime()),
        "结束时间不能早于开始时间");

//    // 判断是否存在该代金券参与秒杀活动
//    SeckillVouchers sec = secKillVouchersMapper.selectVoucher(seckillVouchers.getFkVoucherId());
//    AssertUtil.isTrue(sec != null, "该优惠卷已经参与过活动");
//    // 插入数据库
//    secKillVouchersMapper.save(seckillVouchers);
    String key = RedisKeyConstant.seckill_vouchers.getKey() + seckillVouchers.getFkVoucherId();
    final Map entries = redisTemplate.opsForHash().entries(key);
    AssertUtil.isTrue(((int) entries.get("amount")) > 0 &&
        !entries.isEmpty(), "该优惠卷已经参与过活动了");

    //插入redis中
    seckillVouchers.setIsValid(1);
    seckillVouchers.setCreateDate(now);
    seckillVouchers.setUpdateDate(now);

    redisTemplate.opsForHash().putAll(key, BeanUtil.beanToMap(seckillVouchers));
  }


  @Transactional(rollbackFor = Exception.class)
  public ResultInfo doSecKill(String access_token, Integer voucherId, String path) {
    // 基本参数的健壮性校验
    AssertUtil.isTrue(voucherId < 1, "请输入要使用的代金券");
    AssertUtil.isNotEmpty(access_token, " 请用户登录");

    // 该代金券是否已经用于抢购活动了， 如果已经用过，就会生成订单
//    SeckillVouchers seckillVouchers = secKillVouchersMapper.selectVoucher(voucherId);
//    AssertUtil.isTrue(seckillVouchers == null, "代金券并未有过抢购");
    String key = RedisKeyConstant.seckill_vouchers.getKey() + voucherId;
    final Map map = redisTemplate.opsForHash().entries(key);
    SeckillVouchers seckillVouchers = BeanUtil
        .mapToBean(map, SeckillVouchers.class, true, null);

    // 代金券的日期
    Date now = new Date();

    AssertUtil.isTrue(now.before(seckillVouchers.getStartTime()), "当前抢购活动还未开始");
    AssertUtil.isTrue(now.after(seckillVouchers.getEndTime()), "抢购活动已经结束");
    // 代金券是否已经卖完
    AssertUtil.isTrue(seckillVouchers.getAmount() < 1, "代金券已经抢购完");

    // 获取登录用户的信息
    String url = oauthServerName + "user/me?access_token={access_token}";

    ResultInfo result = restTemplate.getForObject(url, ResultInfo.class, access_token);
    if (result.getCode() != ApiConstant.SUCCESS_CODE) {
      result.setPath(path);
      return result;
    }
    SignInDinerInfo signInDinerInfo = BeanUtil.fillBeanWithMap(((LinkedHashMap) result.getData()),
        new SignInDinerInfo(), false);

    // 判断该用户是否已经抢过代金券 一个用户针对这个活动只能抢一次
    final VoucherOrders dinerOrder = voucherOrdersMapper
        .findDinerOrder(signInDinerInfo.getId(), seckillVouchers.getFkVoucherId());
    AssertUtil.isTrue(dinerOrder != null, "该用户已经抢过代金券成功，无需再抢");

    String lockName = RedisKeyConstant.lock_key.getKey() + dinerOrder.getFkDinerId();

    String lockKey = redisLock.tryLock(lockName, 20);

    try {
      if (StrUtil.isNotBlank(lockKey)) {
        // 不为空，说明该用户已经抢到了锁。用户执行下单逻辑
        // 用户下单，使用redis分布式锁，保证在高并发下一个用户一次只能抢购一次
        VoucherOrders order = new VoucherOrders();
        order.setFkDinerId(signInDinerInfo.getId());
        order.setFkSeckillId(seckillVouchers.getId());
        order.setFkVoucherId(seckillVouchers.getFkVoucherId());
        // 设置订单编号
        order.setOrderNo(IdUtil.getSnowflake(1, 1).nextIdStr());
        // 设置订单类型
        order.setOrderType(1);
        order.setStatus(0);
        int saved = voucherOrdersMapper.save(order);
        AssertUtil.isTrue(saved == 0, "用户抢购失败");

        // 采用redis扣减库存  redis + lua  保证查库存 和扣减库存放在同一个线程当中
        List<String> keyList = new ArrayList<>();
        keyList.add(key);
        keyList.add("amount");
        Long count = ((Long) redisTemplate.execute(redisScript, keyList));
        AssertUtil.isTrue(count == null || count < 1, "代金券已经抢购完了");
      }
    } catch (Exception e) {
      // 手动回滚事务
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      redisLock.unLock(lockName,lockKey);
      if (e instanceof ParamterException) {
        return ResultInfoUtil.buildError(0, "抢购失败", path);
      }
    }

    return ResultInfoUtil.buildSuccess(path, "用户抢购成功");

  }


}
