package com.imooc.diners.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.diners.service.SendVerifyCodeService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: food-social-contact-parent
 * @description: 发送短信验证码的接口，前端传过来的是手机号
 * @author: Mr.Wang
 * @create: 2022-04-13 17:35
 **/
@RestController
public class SendVerifyCodeController {

  @Resource
  private HttpServletRequest request;

  @Resource
  private SendVerifyCodeService sendVerifyCodeService;


  @GetMapping("send")
  public ResultInfo sendVerifyCode(String mobilePhone) {
    sendVerifyCodeService.sendVerifyCode(mobilePhone);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), "发送短信验证码成功");
  }


}
