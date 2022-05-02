package com.imooc.diners.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.dto.DinnerInfoDTO;
import com.imooc.commons.model.vo.ShortInDinerInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.diners.service.DinnerService;
import io.swagger.annotations.Api;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-02 17:01
 **/

@Api(tags = "食客登录接口")
@RestController
public class DinnersController {

  @Resource
  private HttpServletRequest httpServletRequest;

  @Resource
  private DinnerService dinnerService;



  @PostMapping("register")
  public ResultInfo register(@RequestBody DinnerInfoDTO  dinnerInfoDTO) {
    return dinnerService.register(dinnerInfoDTO, httpServletRequest.getServletPath());
  }


  /**
   *
   * 食客登录功能
   * @param account   账号
   * @param password  密码
   * @return
   */
  @GetMapping("signin")
  public ResultInfo signIn(String account, String password) {
    return dinnerService.signIn(account, password, httpServletRequest.getServletPath());

  }

  /**
   * 校验手机号是否已注册
   *
   * @param phone
   * @return
   */
  @GetMapping("checkphone")
  public ResultInfo checkPhone(String phone) {
    dinnerService.checkPhoneIsRegistered(phone);
    return ResultInfoUtil.buildSuccess(httpServletRequest.getServletPath());
  }

  /**
   * 根据 ids 查询食客信息
   *
   * @param ids
   * @return
   */
  @GetMapping("findByIds")
  public ResultInfo<List<ShortInDinerInfo>> findByIds(String ids) {
    List<ShortInDinerInfo> dinerInfos = dinnerService.findByIds(ids);
    return ResultInfoUtil.buildSuccess(httpServletRequest.getServletPath(), dinerInfos);
  }
}
