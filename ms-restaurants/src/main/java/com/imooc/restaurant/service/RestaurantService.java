package com.imooc.restaurant.service;

import cn.hutool.core.bean.BeanUtil;
import com.imooc.commons.constants.RedisKeyConstant;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.Restaurant;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.restaurant.mapper.RestaurantMapper;
import java.util.LinkedHashMap;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-26 00:07
 **/

@Service
@Slf4j
public class RestaurantService {

  @Resource
  private RestaurantMapper restaurantMapper;
  
  
  
  @Resource
  private RedisTemplate redisTemplate;
  
  
  
  public Restaurant findRestaurantById(Integer restaurantId) {
    // 请选择餐厅
    AssertUtil.isTrue(restaurantId == null, "请选择餐厅查看");
    // 获取 Key
    String key = RedisKeyConstant.restaurants.getKey() + restaurantId;
    // 获取餐厅缓存
    LinkedHashMap restaurantMap = (LinkedHashMap) redisTemplate.opsForHash().entries(key);
    // 如果缓存不存在，查询数据库
    Restaurant restaurant = null;
    if (restaurantMap == null || restaurantMap.isEmpty()) {
      log.info("缓存失效了，查询数据库：{}", restaurantId);
      // 查询数据库
      restaurant = restaurantMapper.findById(restaurantId);
      if (restaurant != null) {
        // 更新缓存
        redisTemplate.opsForHash().putAll(key, BeanUtil.beanToMap(restaurant));
      } else {

      }
    } else {
      restaurant = BeanUtil.fillBeanWithMap(restaurantMap,
          new Restaurant(), false);
    }
    return restaurant;
      
  }

}
