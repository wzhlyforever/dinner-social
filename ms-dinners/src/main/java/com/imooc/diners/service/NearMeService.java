package com.imooc.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.constants.RedisKeyConstant;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.vo.NearMeDinerVO;
import com.imooc.commons.model.vo.ShortInDinerInfo;
import com.imooc.commons.model.vo.SignInDinerInfo;
import com.imooc.commons.utils.AssertUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoRadiusCommandArgs;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sun.security.action.GetLongAction;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-24 22:23
 **/

@Service
public class NearMeService {


  @Resource
  private RedisTemplate redisTemplate;


  @Value("${service.name.ms-oauth-server}")
  private String oauthServerName;


  @Resource
  private RestTemplate restTemplate;

  @Resource
  private DinnerService dinnerService;


  /**
   * 更新用户地理信息
   *
   * @param access_token
   * @param lon
   * @param lat
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateDinnerLocation(String access_token, Float lon, Float lat) {
    // 参数校验
    AssertUtil.isTrue(lon == null, "获取精度失败");
    AssertUtil.isTrue(lat == null, "获取维度失败");
    // 获取登录用户信息
    SignInDinerInfo signInDinerInfo = loadUserByToken(access_token);
    // 获取 key diner:location
    String key = RedisKeyConstant.diner_location.getKey();
    // 将用户地址存入 Redis
    Integer dinerId = signInDinerInfo.getId();
    RedisGeoCommands.GeoLocation geoLocation = new GeoLocation(dinerId, new Point(lon, lat));
    redisTemplate.opsForGeo().add(key, geoLocation);   // geoADD


  }

  /**
   * 查找附近的人
   *
   * @param access_token
   * @param radius
   * @param lon
   * @param lat
   * @return
   */
  public List<NearMeDinerVO> findNearMe(String access_token, Integer radius, Float lon, Float lat) {
    // 获取登录用户的信息 id
    SignInDinerInfo diner = loadUserByToken(access_token);
    Integer dinerId = diner.getId();
    // 判断 前端是否传来，没有传 默认是 1000km
    if (radius == null) {
      // 默认是 1000 m 半径
      radius = 1000;
    }
    // 判断是lon lat 是否为空， 为空 则 从redis中获取
    Point point = null;
    String key = RedisKeyConstant.diner_location.getKey();
    if (lon == null && lat == null) {
      // redis中获取 lon ，lat
      List<Point> position = redisTemplate.opsForGeo().position(key, dinerId);
      AssertUtil.isTrue(position == null || position.isEmpty(), "retrieve Geo locatino fail");
      point = position.get(0);
    } else {
      point = new Point(lon, lat);
    }

    // 调用 GEORADIUS diner:location 121.446617 31.205593 3000 m WITHDIST ASC 获取附近的人的信息
    GeoRadiusCommandArgs args = GeoRadiusCommandArgs.newGeoRadiusArgs().limit(10).includeDistance()
        .sortAscending();


    Distance distance = new Distance(radius, DistanceUnit.METERS);
    Circle circle = new Circle(point, distance);    // point, distance
    GeoResults<GeoLocation> geoResults = redisTemplate.opsForGeo().radius(key, circle, args);
    // 封装map
    Map<Integer, NearMeDinerVO> nearMeDinerVOMap = Maps.newLinkedHashMap();
    geoResults.forEach(
        geoLocationGeoResult -> {
          NearMeDinerVO nearMeDinerVO = new NearMeDinerVO();
          @NonNull final GeoLocation<Integer> content = geoLocationGeoResult.getContent();
          nearMeDinerVO.setId(content.getName());
          Double distances = geoLocationGeoResult.getDistance().getValue();
          String  distanceStr = NumberUtil.roundStr(distances, 1);
          nearMeDinerVO.setDistance(distanceStr);
          nearMeDinerVOMap.put(content.getName(), nearMeDinerVO);

        }
    );
    Integer[] ids = nearMeDinerVOMap.keySet().toArray(new Integer[]{});
    List<ShortInDinerInfo> dinerInfos = dinnerService.findByIds(StrUtil.join(",", ids));

    for (ShortInDinerInfo dinerInfo : dinerInfos) {
      NearMeDinerVO nearMeDinerVO = nearMeDinerVOMap.get(dinerInfo.getId());
      nearMeDinerVO.setAvatarUrl(dinerInfo.getAvatarUrl());
      nearMeDinerVO.setNickname(dinerInfo.getNickname());
    }

    return CollectionUtil.newArrayList(nearMeDinerVOMap.values());


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
