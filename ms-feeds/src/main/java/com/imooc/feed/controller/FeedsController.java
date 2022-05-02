package com.imooc.feed.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.Feeds;
import com.imooc.commons.model.vo.FeedsVO;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.feed.service.FeedService;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-19 18:21
 **/
@RestController
public class FeedsController {


  @Resource
  private HttpServletRequest request;

  @Resource
  private FeedService feedService;


  /**
   * 添加 Feed
   *
   * @param feeds
   * @param access_token
   * @return
   */
  @PostMapping
  public ResultInfo<String> create(@RequestBody Feeds feeds, String access_token) {
    feedService.create(feeds, access_token);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), "添加成功");
  }


  /**
   * 删除 Feed
   *
   * @param id
   * @param access_token
   * @return
   */
  @DeleteMapping("{id}")
  public ResultInfo delete(@PathVariable Integer id, String access_token) {
    feedService.delete(id, access_token);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), "删除成功");
  }

  /**
   * 变更 Feed
   *   你变成了粉丝   followingDinerId  关注的好友的id
   * @return
   */
  @PostMapping("updateFollowingFeeds/{followingDinerId}")
  public ResultInfo addFollowingFeeds(@PathVariable Integer followingDinerId,
      String access_token, @RequestParam int type) {
    feedService.addFollowingFeed(followingDinerId, access_token, type);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), "操作成功");
  }

  /**
   * 分页获取关注的 Feed 数据
   *
   * @param page
   * @param access_token
   * @return
   */
  @GetMapping("{page}")
  public ResultInfo selectForPage(@PathVariable Integer page, String access_token) {
    List<FeedsVO> feedsVOS = feedService.selectForPage(page, access_token);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), feedsVOS);
  }

}
