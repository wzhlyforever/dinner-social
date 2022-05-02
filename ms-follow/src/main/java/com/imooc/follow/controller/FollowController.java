package com.imooc.follow.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.follow.service.FollowService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-18 20:28
 **/

@RestController
public class FollowController {



  @Resource
  private HttpServletRequest request;

  @Resource
  private FollowService followService;


  /**
   * 关注/取关
   *
   * @param followDinerId 关注的食客ID
   * @param isFollowed    是否关注 1=关注 0=取消
   * @param access_token  登录用户token
   * @return
   */
  @PostMapping("/{followDinerId}")
  public ResultInfo follow(@PathVariable Integer followDinerId,
      @RequestParam int isFollowed,
      String access_token) {
    ResultInfo resultInfo = followService.follow(access_token,
        isFollowed, followDinerId, request.getServletPath());
    return resultInfo;
  }


  /**
   * 共同关注列表
   *
   * @param dinerId   6
   * @param access_token  5
   * @return
   */
  @GetMapping("commons/{dinerId}")
  public ResultInfo findCommonsFriends(@PathVariable Integer dinerId,
      String access_token) {
    return followService.findCommonsFriends(dinerId, access_token, request.getServletPath());
  }

  /**
   * 获取粉丝列表
   *
   * @param dinerId
   * @return
   */
  @GetMapping("followers/{dinerId}")
  public ResultInfo findFollowers(@PathVariable Integer dinerId) {
    return ResultInfoUtil.buildSuccess(request.getServletPath(),
        followService.findFollowers(dinerId));
  }


}
