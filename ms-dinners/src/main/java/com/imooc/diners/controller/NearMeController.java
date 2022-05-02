package com.imooc.diners.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.vo.NearMeDinerVO;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.diners.service.NearMeService;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-24 22:19
 **/
@RestController
@RequestMapping("nearme")
public class NearMeController {


  @Resource
  private HttpServletRequest request;

  @Resource
  private NearMeService nearMeService;

  @PostMapping
  public ResultInfo updateDinnerLocation(String access_token, @RequestParam float lon,
      @RequestParam float lat) {
    nearMeService.updateDinnerLocation(access_token, lon, lat);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), "更新位置成功");
  }

  /**
   * 获取附近的人
   *
   * @param access_token
   * @param radius
   * @param lon
   * @param lat
   * @return
   */
  @GetMapping
  public ResultInfo<List<NearMeDinerVO>> nearMe(String access_token,
      Integer radius,
      Float lon, Float lat) {
    List<NearMeDinerVO> result = nearMeService.findNearMe(access_token, radius, lon, lat);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), result);
  }







}
