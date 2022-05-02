package com.imooc.commons.constants;

import lombok.Getter;

/**
 * rediskey的枚举类
 */

@Getter
public enum RedisKeyConstant {


  verify_code("verify_code:", "短信验证码"),
  seckill_vouchers("seckill_vouchers:", "秒杀相关的代金券 key"),
  lock_key("lock_key:", "分布式锁的key"),
  following("following:", "关注集合Key"),           // 我关注了谁
  followers("followers:", "粉丝集合key"),           // 谁关注了我
  following_feeds("following_feeds:", "我关注的好友的FeedsKey"),
  diner_points("diner:points:", "食客积分"),
  diner_location("diner:location","食客地理信息"),
  restaurants("restaurants:", "餐厅数据"),
  restaurant_new_reviews("restaurant:new:reviews:", "餐厅评论Key");


  private String key;
  private String desc;


  /**
   * @param key  redis 的键key
   * @param desc
   */
  RedisKeyConstant(String key, String desc) {
    this.key = key;
    this.desc = desc;
  }
}
