package com.imooc.diners.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.diners.service.SignService;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: food-social-contact-parent
 * @description: 用户签到功能
 * @author: Mr.Wang
 * @create: 2022-04-22 11:30
 **/
@RestController
@RequestMapping("sign")
public class SignController {


  @Resource
  private SignService service;

  @Resource
  private HttpServletRequest request;


  /**
   * 用户签到，补签功能, 返回连续签到次数
   *
   * @param access_token
   * @param date   不填就表示当天
   * @return
   */
  @PostMapping
  public ResultInfo sign(String access_token,@RequestParam(required = false) String date) {

    final int count = service.sign(access_token, date);
    System.out.println(count);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), count
    );



  }


  /**
   * 获取签到次数 默认当月
   *
   * @param access_token
   * @param date
   * @return
   */
  @GetMapping("count")
  public ResultInfo getSignCount(String access_token, String date) {
    Long count = service.getSignCount(access_token, date);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), count);
  }

  /**
   * 获取当月的签到信息
   * @param access_token
   * @param date
   * @return
   */
  @GetMapping
  public ResultInfo getSignInfo(String access_token, String date) {
    Map<String, Boolean> signInfo = service.getSignInfo(access_token, date);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), signInfo);
  }
  




}
