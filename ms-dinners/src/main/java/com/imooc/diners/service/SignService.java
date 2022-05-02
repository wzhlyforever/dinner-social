package com.imooc.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.constants.PointTypesContant;
import com.imooc.commons.exception.ParamterException;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.vo.SignInDinerInfo;
import com.imooc.commons.utils.AssertUtil;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @program: food-social-contact-parent
 * @description: 用户签到功能
 * @author: Mr.Wang
 * @create: 2022-04-22 11:37
 **/
@Service
public class SignService {


  @Value("${service.name.ms-oauth-server}")
  private String oauthServerName;
  @Value("${service.name.ms-points-server}")
  private String pointsServerName;

  @Resource
  private RestTemplate restTemplate;

  @Resource
  private RedisTemplate redisTemplate;


  /**
   * 用户签到、补签功能
   *
   * @param access_token
   * @param dateStr
   * @return
   */
  @Transactional(rollbackFor = Exception.class)
  public int sign(String access_token, String dateStr) {
    // 获取登录用户信息
    SignInDinerInfo dinerInfo = loadUserByToken(access_token);
    // 获取签到日期信息,已经是第几天
    Date date = getDate(dateStr);
    final int day = DateUtil.dayOfMonth(date) - 1;   // 从0 开始 表示第一天
    // 构造 key   user:sign:dinerId:yyyyMM
    final String sginKey = buildSignKey(dinerInfo.getId(), date);

    // 判断是否已经签到 getbit user:sign:1:202011 day
    Boolean isSigned = redisTemplate.opsForValue().getBit(sginKey, day);
    AssertUtil.isTrue(isSigned, "the user is signed");
    // 执行签到
    redisTemplate.opsForValue().setBit(sginKey, day, true);
//    // 获取连续签到天数，并且返回
    final int signCount = getContinuesSignDays(dinerInfo.getId(), date);
    // 获取积分
    int points = addPoints(dinerInfo.getId(), signCount);
    return points;
  }

  /**
   * 签到获取积分
   * @param dinerInfoId
   * @param signCount
   * @return
   */
  private int addPoints(Integer dinerInfoId, Integer signCount) {
    int points = 10;
    if (signCount == 2) {
      points = 20;
    } else if (signCount == 3) {
      points = 30;
    } else if (signCount == 4) {
      points = 40;
    } else {
      points = 50;
    }

    // 调用积分接口添加积分
    // 构建请求头
    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    body.set("dinerId", dinerInfoId);
    body.set("points", points);
    body.set("type", PointTypesContant.sign.getType());

    HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(
        body, header);
    // 发送请求
    ResponseEntity<ResultInfo> result = restTemplate.postForEntity(pointsServerName,
        entity, ResultInfo.class);
    AssertUtil.isTrue(result.getStatusCode() != HttpStatus.OK, "登录失败！");
    ResultInfo resultInfo = result.getBody();
    if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
      // 失败了, 事物要进行回滚
      throw new ParamterException(resultInfo.getMessage(), resultInfo.getCode());
    }
    return points;
  }


  /**
   * 获取用户签到次数
   *
   * @param access_token
   * @param dateStr
   * @return
   */
  public Long getSignCount(String access_token, String dateStr) {

    // 获取登录用户信息
    SignInDinerInfo dinerInfo = loadUserByToken(access_token);
    // 获取签到日期信息,已经是第几天
    Date date = getDate(dateStr);

    // 构造 key   user:sign:dinerId:yyyyMM
    final String sginKey = buildSignKey(dinerInfo.getId(), date);

    // 获取用户签到次数
    return ((Long) redisTemplate.execute(
        ((RedisCallback<Long>) connection -> connection.bitCount(sginKey.getBytes()))

    ));
  }

  /**
   * 获取用户当月的情况
   *
   * @param access_token
   * @param dateStr
   * @return
   */
  public Map<String, Boolean> getSignInfo(String access_token, String dateStr) {
    // 获取登陆用户信息
    SignInDinerInfo dinerInfo = loadUserByToken(access_token);
    Date date = getDate(dateStr);

    // 构建key
    String signKey = buildSignKey(dinerInfo.getId(), date);

    // 获取这个月的总天数
    int dayOfMonth = DateUtil
        .lengthOfMonth(DateUtil.month(date) + 1, DateUtil.isLeapYear(DateUtil.year(date)));

    BitFieldSubCommands bitFieldSubCommands = BitFieldSubCommands.create()
        .get(BitFieldType.unsigned(dayOfMonth)).valueAt(0);
    List<Long> bitField = redisTemplate.opsForValue().bitField(signKey, bitFieldSubCommands);

    Map<String, Boolean> signInfoMap = new TreeMap<>();

    long v = bitField.get(0) == null ? 0 : bitField.get(0);

    for (int i = dayOfMonth; i > 0; i--) {

      /*
        yyyy-MM-DD  :  boolean
      */
      LocalDateTime localDateTime = LocalDateTimeUtil.of(date).withDayOfMonth(i);

      boolean flag = v >> 1 << 1 != v;

      signInfoMap.put(DateUtil.format(localDateTime, "yyyy-MM-dd"), flag);

      v = v >> 1;
    }

    return signInfoMap;
  }

  /**
   * 获取连续签到的天数
   *
   * @param dinnerId
   * @param date
   * @return
   */
  private int getContinuesSignDays(Integer dinnerId, Date date) {

    final int offset = DateUtil.dayOfMonth(date);

    String signKey = buildSignKey(dinnerId, date);

    BitFieldSubCommands bitFieldSubCommands = BitFieldSubCommands.create()
        .get(BitFieldType.unsigned(offset)).valueAt(0);

    final List<Long> bitField = redisTemplate.opsForValue().bitField(signKey, bitFieldSubCommands);

    if (bitField == null || bitField.isEmpty()) {
      return 0;
    }

    int signCount = 0;
    long v = bitField.get(0) == null ? 0 : bitField.get(0);
    for (int i = offset; i > 0; i--) {

      if (v >> 1 << 1 == v) {
        // 说明未签到  // 低位为0 并且不是当天 则表示连续签到断签了
        if (i != offset) {
          break;
        }
      } else {
        signCount++;
      }
      v = v >> 1;

    }
    return signCount;


  }

  private String buildSignKey(Integer dinnerId, Date date) {
    return String.format("user:sign:%d:%s", dinnerId, DateUtil.format(date, "yyyyMM"));
  }

  private Date getDate(String dateStr) {
    if (dateStr == null) {
      return new Date();
    }

    try {
      return DateUtil.parseDate(dateStr);
    } catch (Exception e) {
      throw new ParamterException("请传入yyyy-MM-dd的日期格式");
    }
  }


  /**
   * 根据token 获取用户信息
   *
   * @param access_token
   * @return
   */
  private SignInDinerInfo loadUserByToken(String access_token) {

    String url = oauthServerName + "user/me?access_token={access_token}";
    ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, access_token);
    AssertUtil.isTrue(resultInfo.getCode() != ApiConstant.SUCCESS_CODE, "无法获取登录用户信息");

    SignInDinerInfo dinner = BeanUtil
        .fillBeanWithMap(((LinkedHashMap) resultInfo.getData()), new SignInDinerInfo(), false,
            null);

    return dinner;

  }


}
