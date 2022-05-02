package com.imooc.commons.model.pojo;

import com.imooc.commons.model.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-18 16:39
 **/

@Getter
@Setter
@ApiModel(description = "食客关注列表")
public class Follow extends BaseModel {

  @ApiModelProperty("用户ID")
  private int dinerId;
  @ApiModelProperty("关注用户ID")
  private Integer followDinerId;

}
