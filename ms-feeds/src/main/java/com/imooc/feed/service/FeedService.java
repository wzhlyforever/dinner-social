package com.imooc.feed.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.constants.RedisKeyConstant;
import com.imooc.commons.exception.ParamterException;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.Feeds;
import com.imooc.commons.model.vo.FeedsVO;
import com.imooc.commons.model.vo.ShortInDinerInfo;
import com.imooc.commons.model.vo.SignInDinerInfo;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.feed.mapper.FeedsMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-19 16:23
 **/
@Service
public class FeedService {


  @Resource
  private FeedsMapper feedsMapper;

  @Value("${service.name.ms-oauth-server}")
  private String oauthServerName;

  @Value("${service.name.ms-follow-server}")
  private String followServerName;

  @Value("${service.name.ms-dinners-server}")
  private String dinnersServerName;

  @Resource
  private RestTemplate restTemplate;

  @Resource
  private RedisTemplate redisTemplate;


  /**
   * 添加 Feed
   *
   * @param feeds
   * @param access_token
   */
  @Transactional(rollbackFor = Exception.class)
  public void create(Feeds feeds, String access_token) {
    AssertUtil.isNotEmpty(feeds.getContent(), "please input content");
    AssertUtil.isTrue(feeds.getContent().length() > 256, "the feed content is too long");
    // 获取登录用户信息
    SignInDinerInfo dinner = loadUserByToken(access_token);

    // 补充feed信息
    feeds.setFkDinerId(dinner.getId());

    int count = feedsMapper.save(feeds);
    AssertUtil.isTrue(count == 0, " add fail!");
    // 获取该用户的粉丝集合
    List<Integer> followers = findFollowers(dinner.getId());

    followers.forEach(follower -> {
      final long now = System.currentTimeMillis();
      String key = RedisKeyConstant.following_feeds.getKey() + follower;
      redisTemplate.opsForZSet().add(key, feeds.getId(), now);
    });
  }


  /**
   * 删除 Feed
   *
   * @param id
   * @param access_token
   */
  @Transactional(rollbackFor = Exception.class)
  public void delete(Integer id, String access_token) {
    AssertUtil.isTrue(id < 1 || id == null, " please input feed content");

    SignInDinerInfo dinner = loadUserByToken(access_token);

    Feeds feeds = feedsMapper.findById(id);
    AssertUtil.isTrue(feeds == null, "feed deleted"); // feed 已经删除

    // 判断是不是自己发的feed，不是自己的不能删除
    AssertUtil.isTrue(feeds.getFkDinerId() != dinner.getId(), "no permission to delete");

    // 删除
    int count = feedsMapper.delete(id);
    if (count == 0) {
      return;
    }

    // 获取该用户的粉丝集合
    List<Integer> followers = findFollowers(dinner.getId());

    followers.forEach(follower -> {
      String key = RedisKeyConstant.following_feeds.getKey() + follower;
      redisTemplate.opsForZSet().remove(key, feeds.getId());
    });

  }

  /**
   * 变更feed
   *
   * @param followingDinerId
   * @param access_token
   * @param type
   */
  public void addFollowingFeed(Integer followingDinerId, String access_token, int type) {

    AssertUtil.isNotNull(followingDinerId, "please following dinners");

    SignInDinerInfo dinner = loadUserByToken(access_token);

    // 获取要关注好友的所有feed
    List<Feeds> feedsList = feedsMapper.findByDinerId(followingDinerId);
    String key = RedisKeyConstant.following_feeds.getKey() + dinner.getId();

    if (type == 0) {
      // 取关
      List<Integer> feedIds = feedsList.stream()
          .map(feed -> feed.getId())
          .collect(Collectors.toList());
      redisTemplate.opsForZSet().remove(key, feedIds.toArray(new Integer[]{}));
    } else {
      // 关注
      Set<ZSetOperations.TypedTuple> typedTuples =
          feedsList.stream()
              .map(feed -> new DefaultTypedTuple<>(feed.getId(),
                  (double) feed.getUpdateDate().getTime()))
              .collect(Collectors.toSet());
      redisTemplate.opsForZSet().add(key, typedTuples);
    }

  }

  /**
   * @param page         第几页
   * @param access_token
   * @return
   */
  public List<FeedsVO> selectForPage(Integer page, String access_token) {

    if (page == null) {
      page = 1;
    }

    SignInDinerInfo diner = loadUserByToken(access_token);
    // 我所要获取关注的好友的集合
    String key = RedisKeyConstant.following_feeds.getKey() + diner.getId();
    int start = (page - 1) * ApiConstant.PAGE_SIZE;
    int end = page * ApiConstant.PAGE_SIZE - 1;
    Set<Integer> feedIds = redisTemplate.opsForZSet().range(key, start, end);
    if (feedIds.isEmpty()) {
      return CollectionUtil.newArrayList();   // 返回一个null 数组
    }

    List<Feeds> feedsList = feedsMapper.findFeedsByIds(feedIds);

    List<Integer> dinersIds = new ArrayList<>();

    List<FeedsVO> feedsVOS = feedsList.stream().map(
        feeds -> {
          FeedsVO feedsVO = new FeedsVO();
          BeanUtils.copyProperties(feeds, feedsVO);
          dinersIds.add(feeds.getFkDinerId());
          return feedsVO;
        }
    ).collect(Collectors.toList());

    // 获取关注的好友集合
    String url = dinnersServerName + "findByIds?access_token={access_token}&ids={ids}";

    ResultInfo dinnersInfo = restTemplate
        .getForObject(url, ResultInfo.class, access_token, dinersIds);

    if (dinnersInfo.getCode() != ApiConstant.SUCCESS_CODE) {
      throw new ParamterException(dinnersInfo.getMessage(), dinnersInfo.getCode());
    }

    ArrayList<LinkedHashMap> dinnersInfoMaps = (ArrayList) dinnersInfo.getData();

    Map<Integer, ShortInDinerInfo> map = dinnersInfoMaps.stream().collect(
        Collectors.toMap(
            dinner -> (Integer) dinner.get("id"),

            dinner -> BeanUtil.fillBeanWithMap(dinner, new ShortInDinerInfo(), true)

        )
    );

    feedsVOS.forEach(
        feedsVO -> feedsVO.setDinerInfo(map.get(feedsVO.getFkDinerId()))
    );

    return feedsVOS;


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

  /**
   * 获取粉丝 id 集合
   *
   * @param dinerId
   * @return
   */
  private List<Integer> findFollowers(Integer dinerId) {
    String url = followServerName + "followers/" + dinerId;
    ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class);
    if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
      throw new ParamterException(resultInfo.getMessage(), resultInfo.getCode());
    }
    List<Integer> followers = (List<Integer>) resultInfo.getData();
    return followers;
  }


}
