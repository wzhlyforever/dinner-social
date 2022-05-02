package com.imooc.restaurant.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.Restaurant;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.restaurant.service.RestaurantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class RestaurantController {

  @Resource
  private RestaurantService restaurantService;
  @Resource
  private HttpServletRequest request;

  /**
   * 根据餐厅 ID 查询餐厅数据
   *
   * @param restaurantId
   * @return
   */
  @GetMapping("detail/{restaurantId}")
  public ResultInfo<Restaurant> findById(@PathVariable Integer restaurantId) {
    Restaurant restaurant = restaurantService.findRestaurantById(restaurantId);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), restaurant);
  }
}
