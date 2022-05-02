package com.imooc.restaurant.service;

import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.Maps;
import com.imooc.commons.constants.RedisKeyConstant;
import com.imooc.commons.model.pojo.Restaurant;
import com.imooc.restaurant.RestaurantApplicationTest;
import com.imooc.restaurant.mapper.RestaurantMapper;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-25 23:25
 **/
@Slf4j
public class RestaurantTest extends RestaurantApplicationTest {


  @Resource
  private RedisTemplate redisTemplate;
  @Resource
  private RestaurantMapper restaurantMapper;

  // 逐行插入
  @Test
  void testSyncForHash() {
    List<Restaurant> restaurants = restaurantMapper.findAll();
    long start = System.currentTimeMillis();
    restaurants.forEach(restaurant -> {
      Map<String, Object> restaurantMap = BeanUtil.beanToMap(restaurant);
      String key = RedisKeyConstant.restaurants.getKey() + restaurant.getId();
      redisTemplate.opsForHash().putAll(key, restaurantMap);
    });
    long end = System.currentTimeMillis();
    log.info("执行时间：{}", end - start); // 执行时间：94218
  }

  @Test
  void testSyncForHashPipeline() {
    // 从数据库中取出数据 利用管道pipline
    List<Restaurant> restaurants = restaurantMapper.findAll();
    long start = System.currentTimeMillis();

    List<Long> list = redisTemplate.executePipelined((RedisCallback<Long>) connection -> {
      for (Restaurant restaurant : restaurants) {
        try {
          String key = RedisKeyConstant.restaurants.getKey() + restaurant.getId();
          Map<String, Object> restaurantMap = BeanUtil.beanToMap(restaurant);
          // key
          StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
          // value
          Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

          Map<byte[], byte[]> restaurantStringMap = Maps.newHashMap();
          restaurantMap.forEach((k, v) -> {
            restaurantStringMap.put(stringRedisSerializer.serialize(k), jackson2JsonRedisSerializer.serialize(v));
          });
          connection.hMSet(stringRedisSerializer.serialize(key), restaurantStringMap);
        } catch (Exception e) {
          log.info(restaurant.toString());
          continue;
        }
      }
      return null;
    });
    long end = System.currentTimeMillis();
    log.info("执行时间：{}", end - start); // 执行时间：22629
  }


}
