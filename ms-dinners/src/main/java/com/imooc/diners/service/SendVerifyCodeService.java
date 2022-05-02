package com.imooc.diners.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.imooc.commons.constants.RedisKeyConstant;
import com.imooc.commons.utils.AssertUtil;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-13 17:37
 **/
@Service
public class SendVerifyCodeService {


  @Resource
  private RedisTemplate<String, String> redisTemplate;
  

  public void sendVerifyCode(String mobilePhone) {

    // 对电话进行非空判断
    AssertUtil.isNotEmpty(mobilePhone, "手机号不能为空");

    // 从redis数据库中查看是否有 该电话对应的短信验证码
    if (!checkHasVerifyCode(mobilePhone)) {
        // 返回false 说明已经有验证码 直接返回
        return;
    }
    // 调用第三方云服务
    String code = RandomUtil.randomNumbers(6);

    // 存入到redis中
    redisTemplate.opsForValue().set(RedisKeyConstant.verify_code.getKey() + mobilePhone, code, 3600,
        TimeUnit.SECONDS);

  }

  /**
   * 检查redis中是否存在短信验证码
   *
   * @param mobilePhone 手机号
   * @return
   */
  private boolean checkHasVerifyCode(String mobilePhone) {
    String code = redisTemplate.opsForValue()
        .get(RedisKeyConstant.verify_code.getKey() + mobilePhone);
    return StrUtil.isBlank(code);
  }

  /**
   * 根据手机号，获取短信验证码
   *
   * @param phone
   * @return
   */
  public String retrieveVerifyCode (String phone) {
    String code =redisTemplate.opsForValue().get(RedisKeyConstant.verify_code.getKey() + phone);
    return code;
  }


}
