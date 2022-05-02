package com.imooc.follow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.constants.RedisKeyConstant;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.Follow;
import com.imooc.commons.model.vo.ShortInDinerInfo;
import com.imooc.commons.model.vo.SignInDinerInfo;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.follow.mapper.FollowMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-18 20:28
 **/
@Service
public class FollowService {


  @Resource
  private FollowMapper followMapper;

  @Value("${service.name.ms-oauth-server}")
  private String oauthServerName;

  @Value("${service.name.ms-diners-server}")
  private String dinersServerName;

  @Value("${service.name.ms-feeds-server}")
  private String feedServerName;

  @Resource
  private RedisTemplate redisTemplate;

  @Resource
  private RestTemplate restTemplate;


  @Transactional(rollbackFor = Exception.class)
  public ResultInfo findCommonsFriends(Integer dinerId, String access_token, String path) {

    AssertUtil.isNotNull(dinerId, "请选择要查看的好友");

    SignInDinerInfo loginDinner = loadUserByToken(access_token);

    String key = RedisKeyConstant.following.getKey() + loginDinner.getId();
    String followKey = RedisKeyConstant.following.getKey() + dinerId;

    Set<Integer> dinerIds = redisTemplate.opsForSet().intersect(key, followKey);

    // 没有
    if (dinerIds == null || dinerIds.isEmpty()) {
      return ResultInfoUtil.buildSuccess(path, new ArrayList<ShortInDinerInfo>());
    }
    // 调用食客服务根据 ids 查询食客信息
    ResultInfo resultInfo = restTemplate
        .getForObject(dinersServerName + "findByIds?access_token={access_token}&ids={ids}"
            , ResultInfo.class, access_token, StrUtil.join(",", dinerIds)
        );

    if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
      resultInfo.setPath(path);
      return resultInfo;
    }

    // 处理结果集
    ArrayList<LinkedHashMap> data = (ArrayList) resultInfo.getData();
    final List<ShortInDinerInfo> shortInDinerInfos = data.stream()
        .map(dinner -> BeanUtil.fillBeanWithMap(dinner, new ShortInDinerInfo(), false))
        .collect(Collectors.toList());

    return ResultInfoUtil.buildSuccess(path, shortInDinerInfos);

  }

  /**
   * 关注/ 取关
   *
   * @param access_token     登录用户token
   * @param followed         1 关注   0 取消
   * @param follow_dinner_id 关注的好友id
   * @return
   */
  public ResultInfo follow(String access_token, int followed,
      Integer follow_dinner_id, String path) {

    AssertUtil.isTrue(follow_dinner_id == null || follow_dinner_id < 1, "请选择要关注的人");
    SignInDinerInfo dinnerInfo = loadUserByToken(access_token);
    Follow follow = followMapper.selectFollow(dinnerInfo.getId(), follow_dinner_id);

    if (follow == null && followed == 1) {
      // 表示添加关注
      int count = followMapper.save(dinnerInfo.getId(), follow_dinner_id);
      if (count == 1) {
        addFollowToSet(dinnerInfo.getId(), follow_dinner_id);
        // 保存 Feed
        sendSaveOrRemoveFeed(follow_dinner_id, access_token, 1);
      }
      return ResultInfoUtil.buildSuccess(path, "关注成功");
    }

    if (follow != null && follow.getIsValid() == 1 && followed == 0) {
      // 表示取消关注
      int update = followMapper.update(follow.getId(), followed);
      if (update == 1) {
        removeFollowFromSet(dinnerInfo.getId(), follow_dinner_id);
        // 移除feed
        sendSaveOrRemoveFeed(follow_dinner_id, access_token, 0);
      }
      return ResultInfoUtil.buildSuccess(path, "取关成功");
    }

    if (follow != null && follow.getIsValid() == 0 && followed == 1) {
      // 表示重新关注
      int update = followMapper.update(follow.getId(), followed);
      if (update == 1) {
        addFollowToSet(
            dinnerInfo.getId(), follow_dinner_id);

        sendSaveOrRemoveFeed(follow_dinner_id, access_token, 1);
      }
      return ResultInfoUtil.buildSuccess(path, "重新关注成功");
    }
    return ResultInfoUtil.buildSuccess(path, "operation success!");

  }

  private void sendSaveOrRemoveFeed(Integer follow_dinner_id, String access_token, int type) {

    // 构造请求头
    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    // 构造请求体
    LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.set("type", type);

    HttpEntity entity = new HttpEntity(body, header);

    String feedsUpdateUrl = feedServerName + "updateFollowingFeeds/"
        + follow_dinner_id + "?access_token=" + access_token;

    restTemplate.postForEntity(feedsUpdateUrl, entity, ResultInfo.class);
  }



  /**
   * 从关注 粉丝集合中移除该好友
   *
   * @param dinnerInfoId
   * @param follow_dinner_id
   */
  private void removeFollowFromSet(Integer dinnerInfoId, Integer follow_dinner_id) {
    redisTemplate.opsForSet()
        .remove(RedisKeyConstant.following.getKey() + dinnerInfoId, follow_dinner_id);
    redisTemplate.opsForSet()
        .remove(RedisKeyConstant.followers.getKey() + follow_dinner_id, dinnerInfoId);

  }

  /**
   * 更新关注 粉丝集合列表
   *
   * @param dinnerId
   * @param follow_dinner_id
   */
  private void addFollowToSet(Integer dinnerId, Integer follow_dinner_id) {
    redisTemplate.opsForSet().add(RedisKeyConstant.following.getKey() + dinnerId, follow_dinner_id);
    redisTemplate.opsForSet().add(RedisKeyConstant.followers.getKey() + follow_dinner_id, dinnerId);

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
   * 获取粉丝列表
   *
   * @param dinerId
   * @return
   */
  public Set<Integer> findFollowers(Integer dinerId) {
    AssertUtil.isNotNull(dinerId, "请选择要查看的用户");
    Set<Integer> followers = redisTemplate.opsForSet()
        .members(RedisKeyConstant.followers.getKey() + dinerId);
    return followers;
  }
}