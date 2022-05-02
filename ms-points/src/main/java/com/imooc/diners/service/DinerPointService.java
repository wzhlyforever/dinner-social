package com.imooc.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.constants.RedisKeyConstant;
import com.imooc.commons.exception.ParamterException;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.DinerPoints;
import com.imooc.commons.model.vo.DinerPointsRankVO;
import com.imooc.commons.model.vo.ShortInDinerInfo;
import com.imooc.commons.model.vo.SignInDinerInfo;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.diners.mapper.DinerPointsMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-22 23:41
 **/

@Service
public class DinerPointService {


  @Resource
  private DinerPointsMapper dinerPointsMapper;
  @Value("${service.name.ms-oauth-server}")
  private String oauthServerName;

  @Value("${service.name.ms-dinners-server}")
  private String dinnersServerName;
  @Resource
  private RestTemplate restTemplate;
  @Resource
  private RedisTemplate redisTemplate;


  /**
   * 添加积分
   *
   * @param dinerId 食客ID
   * @param points  积分
   * @param types   类型 0=签到，1=关注好友，2=添加Feed，3=添加商户评论
   */
  @Transactional(rollbackFor = Exception.class)
  public void addPoints(Integer dinerId, Integer points, Integer types) {
    // 基本参数校验
    AssertUtil.isTrue(dinerId == null || dinerId < 1, "食客不能为空");
    AssertUtil.isTrue(points == null || points < 1, "积分不能为空");
    AssertUtil.isTrue(types == null, "请选择对应的积分类型");

    // 插入数据库
    DinerPoints dinerPoints = new DinerPoints();
    dinerPoints.setFkDinerId(dinerId);
    dinerPoints.setPoints(points);
    dinerPoints.setTypes(types);
    dinerPointsMapper.save(dinerPoints);

    // 将积分保存到 Redis
    redisTemplate.opsForZSet().incrementScore(
        RedisKeyConstant.diner_points.getKey(), dinerId, points);
  }

  /**
   * topN  排行榜功能  ------------ mysql
   *
   * @param access_token
   * @return
   */
  public List<DinerPointsRankVO> findDinerPointRank(String access_token) {
    // 获取登录用户信息
    SignInDinerInfo dinerInfo = loadUserByToken(access_token);

    List<DinerPointsRankVO> topN = dinerPointsMapper.findTopN(20);

    if (topN == null) {
      return CollectionUtil.newArrayList();
    }

    /*
      构造一个map key:用户id value: 积分信息
     */
    LinkedHashMap<Integer, DinerPointsRankVO> rankMap = new LinkedHashMap<>();
    for (int i = 0; i < topN.size(); i++) {
      rankMap.put(topN.get(i).getId(), topN.get(i));

    }

    if (rankMap.containsKey(dinerInfo.getId())) {
      rankMap.get(dinerInfo.getId()).setIsMe(1);
      return CollectionUtil.newArrayList(rankMap.values());
    }

    // 我不在排行榜上 就加入到末尾
    DinerPointsRankVO me = dinerPointsMapper.findDinerRank(dinerInfo.getId());
    me.setIsMe(1);
    topN.add(me);
    return topN;


  }

  /**
   * topN  排行榜功能  ------------ redis
   *
   * @param access_token
   * @return
   */
  public List<DinerPointsRankVO> findDinerPointRankFromRedis(String access_token) {

    // 获取登录用户信息
    SignInDinerInfo dinerInfo = loadUserByToken(access_token);

    // 从redis中获取top20 积分排行榜   zrevrange diner:points 0 19 withscores
    Set<ZSetOperations.TypedTuple<Integer>> ranksWithScore = redisTemplate.opsForZSet()
        .reverseRangeWithScores(RedisKeyConstant.diner_points.getKey(), 0, 19);

    if (ranksWithScore.isEmpty() || ranksWithScore == null) {
      return CollectionUtil.newArrayList();
    }

    ArrayList<Integer> rankDinerIds = Lists.newArrayList();
    LinkedHashMap<Integer, DinerPointsRankVO> rankInfoMap = new LinkedHashMap<>();

    int rank = 1;
    for (TypedTuple<Integer> rankWithScore : ranksWithScore) {

      DinerPointsRankVO rankVO = new DinerPointsRankVO();
      rankVO.setTotal(rankWithScore.getScore().intValue());
      rankVO.setId(rankWithScore.getValue());
      rankVO.setRanks(rank);
      rankDinerIds.add(rankWithScore.getValue());
      rankInfoMap.put(rankWithScore.getValue(), rankVO);
      rank++;
    }
    // 此时rankDinerIds 已经有用户id
    String url = dinnersServerName + "findByIds?access_token={access_token}&ids={ids}";
    ResultInfo resultInfo = restTemplate
        .getForObject(url, ResultInfo.class, access_token, StrUtil.join(",", rankDinerIds));

    if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
      throw new ParamterException(resultInfo.getMessage(), resultInfo.getCode());
    }

    List<LinkedHashMap> rankDinnersInfo = (List<LinkedHashMap>) resultInfo.getData();
    for (LinkedHashMap rankDinnerInfo : rankDinnersInfo) {
      ShortInDinerInfo shortInDinerInfo = BeanUtil
          .fillBeanWithMap(rankDinnerInfo, new ShortInDinerInfo(), false);

      DinerPointsRankVO rankVO = rankInfoMap.get(shortInDinerInfo.getId());
      rankVO.setNickname(shortInDinerInfo.getNickname());
      rankVO.setAvatarUrl(shortInDinerInfo.getAvatarUrl());

    }

    // ranInfoMap 上面已经有值了
    if (rankInfoMap.containsKey(dinerInfo.getId())) {
      rankInfoMap.get(dinerInfo.getId()).setIsMe(1);
      return CollectionUtil.newArrayList(rankInfoMap.values());
    }

    // 不在排行榜当中就追加到末尾
    Long myRank = redisTemplate.opsForZSet()
        .reverseRank(RedisKeyConstant.diner_points.getKey(), dinerInfo.getId());

    if (myRank != null) {
      DinerPointsRankVO myRankVO = new DinerPointsRankVO();
      BeanUtil.copyProperties(dinerInfo, myRankVO, true);
      myRankVO.setRanks(myRank.intValue() + 1);
      Double points = redisTemplate.opsForZSet()
          .score(RedisKeyConstant.diner_points.getKey(), dinerInfo.getId());

      myRankVO.setTotal(points.intValue());
      myRankVO.setIsMe(1);
      rankInfoMap.put(dinerInfo.getId(), myRankVO);
    }

    return Lists.newArrayList(rankInfoMap.values());

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
