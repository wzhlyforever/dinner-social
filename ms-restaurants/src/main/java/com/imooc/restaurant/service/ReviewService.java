package com.imooc.restaurant.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.constants.RedisKeyConstant;
import com.imooc.commons.exception.ParamterException;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.Restaurant;
import com.imooc.commons.model.pojo.Reviews;
import com.imooc.commons.model.vo.ReviewsVO;
import com.imooc.commons.model.vo.ShortInDinerInfo;
import com.imooc.commons.model.vo.SignInDinerInfo;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.restaurant.mapper.ReviewMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-26 14:23
 **/
@Service
public class ReviewService {


  @Resource
  private ReviewMapper reviewMapper;

  @Value("${service.name.ms-oauth-server}")
  private String oauthServerName;
  @Value("${service.name.ms-dinners-server}")
  private String dinnerServerName;

  @Resource
  private RestTemplate restTemplate;

  @Resource
  private RedisTemplate redisTemplate;

  @Resource
  private RestaurantService restaurantService;


  /**
   * 新增评论
   *
   * @param restaurantId
   * @param access_token
   * @param content
   * @param likeIt
   */
  public void addReview(Integer restaurantId, String access_token, String content, int likeIt) {

    AssertUtil.isTrue(restaurantId == null, "please input restaurantId");
    AssertUtil.isTrue(content == null, "content not null!");
    AssertUtil.isTrue(content.length() > 800, " the content is too long");

    // 该餐厅是否存在
    final Restaurant restaurant = restaurantService.findRestaurantById(restaurantId);
    AssertUtil.isTrue(restaurant == null, "the restaurant is not exists");

    SignInDinerInfo dinerInfo = loadSignInDinerInfo(access_token);

    Reviews reviews = new Reviews();
    reviews.setContent(content);
    reviews.setFkDinerId(dinerInfo.getId());
    reviews.setFkRestaurantId(restaurantId);
    reviews.setLikeIt(likeIt);

    final int count = reviewMapper.saveReviews(reviews);
    if (count == 0) {
      return;
    }

    String key = RedisKeyConstant.restaurant_new_reviews.getKey() + restaurantId;
    redisTemplate.opsForList().leftPush(key, reviews);
  }


  /**
   * 获取餐厅最新评论 10条
   *
   * @param restaurantId
   * @param access_token
   * @return
   */
  public List<ReviewsVO> findNewReviews(Integer restaurantId, String access_token) {
    AssertUtil.isNotNull(restaurantId, " restaurantId is not null");

    String key = RedisKeyConstant.restaurant_new_reviews.getKey() + restaurantId;
    List<Reviews> reviews = redisTemplate.opsForList().range(key, 0, 9);

    if (reviews == null || reviews.isEmpty()) {
      return CollectionUtil.newArrayList();
    }

    List<Integer> dinerIds = Lists.newArrayList();
    List<ReviewsVO> reviewVOS = Lists.newArrayList();

    reviews.forEach(
        review -> {
          dinerIds.add(review.getFkDinerId());
          ReviewsVO reviewsVO = new ReviewsVO();
          BeanUtil.copyProperties(review, reviewsVO);
          reviewVOS.add(reviewsVO);
        }
    );

    // dinerIds 和 reviewVOS 都有数据 调用食客服务，获取用户信息
    String url = dinnerServerName + "findByIds?access_token={access_token}&ids={ids}";
    ResultInfo resultInfo = restTemplate
        .getForObject(url, ResultInfo.class, access_token, StrUtil.join(",", dinerIds));

    if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
      throw new ParamterException("获取用户信息失败");
    }

    LinkedList<LinkedHashMap> resultInfoData = (LinkedList) resultInfo.getData();

    Map<Integer, ShortInDinerInfo> dinerInfoMap = resultInfoData.stream().collect(
        Collectors.toMap(
            diner -> (int) diner.get("id"),
            diner -> BeanUtil.fillBeanWithMap(diner, new ShortInDinerInfo(), true)
        ));

    reviewVOS.forEach(
        reviewsVO -> reviewsVO.setDinerInfo(dinerInfoMap.get(reviewsVO.getFkDinerId())));

    return reviewVOS;


  }

  /**
   * 获取登录用户信息
   *
   * @param accessToken
   * @return
   */
  private SignInDinerInfo loadSignInDinerInfo(String accessToken) {
    // 登录校验
    AssertUtil.mustLogin(accessToken);
    // 获取登录用户信息
    String url = oauthServerName + "user/me?access_token={accessToken}";
    ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, accessToken);
    if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
      throw new ParamterException(resultInfo.getMessage(), resultInfo.getCode());
    }
    // 这里的data是一个LinkedHashMap，SignInDinerInfo
    SignInDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
        new SignInDinerInfo(), true);
    if (dinerInfo == null) {
      throw new ParamterException(ApiConstant.NO_LOGIN_MESSAGE, ApiConstant.NO_LOGIN_CODE);

    }
    return dinerInfo;
  }


}
