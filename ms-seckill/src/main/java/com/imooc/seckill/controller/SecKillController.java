package com.imooc.seckill.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.SeckillVouchers;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.seckill.service.SecKillVouchersService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-15 12:52
 **/

@RestController
public class SecKillController {


  @Resource
  private HttpServletRequest request;


  @Resource
  private SecKillVouchersService secKillVouchersService;


  @Resource
  private SecKillVouchersService seckillService;

  /**
   * 秒杀下单
   *
   * @param voucherId
   * @param access_token
   * @return
   */
  @PostMapping("{voucherId}")
  public ResultInfo doSeckill(@PathVariable Integer voucherId, String access_token) {
    return seckillService
        .doSecKill(access_token, voucherId, request.getServletPath());
  }


  /**
   * 商家添加秒杀优惠券活动
   *
   * @param vouchers
   * @return
   */
  @PostMapping("add")
  public ResultInfo addSecKillVoucher(@RequestBody SeckillVouchers vouchers) {
    secKillVouchersService.addVourchers(vouchers);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), "添加优惠卷成功");
  }


}
