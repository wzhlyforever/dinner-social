package com.imooc.diners.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.vo.DinerPointsRankVO;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.diners.service.DinerPointService;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: food-social-contact-parent
 * @description: 积分控制层
 * @author: Mr.Wang
 * @create: 2022-04-22 23:33
 **/

@RestController
public class DinerPointsController {


  @Resource
  private HttpServletRequest request;


  @Resource
  private DinerPointService dinerPointService;

  /**
   * 添加积分功能
   *
   * @param dinerId
   * @param points
   * @param type
   * @return
   */
  @PostMapping
  public ResultInfo addPoints(@RequestParam(required = false) Integer dinerId,
      @RequestParam(required = false) Integer points,
      @RequestParam(required = false) Integer type
  ) {

    dinerPointService.addPoints(dinerId, points, type);

    return ResultInfoUtil.buildSuccess(request.getServletPath(), points);

  }

  /**
   * 查询前 20 积分排行榜，同时显示用户排名 -- MySQL
   *
   * @param access_token
   * @return
   */
  @GetMapping
  public ResultInfo findDinerPointsRank(String access_token) {
    List<DinerPointsRankVO> ranks = dinerPointService.findDinerPointRank(access_token);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), ranks);
  }


  /**
   * 查询前 20 积分排行榜，同时显示用户排名 -- Redis
   *
   * @param access_token
   * @return
   */
  @GetMapping("redis")
  public ResultInfo findDinerPointsRankFromRedis(String access_token) {
    List<DinerPointsRankVO> ranks = dinerPointService.findDinerPointRankFromRedis(access_token);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), ranks);
  }

}
