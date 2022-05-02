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
 * @create: 2022-04-19 16:24
 **/
@Getter
@Setter
@ApiModel(description = "feeds信息类")
public class Feeds extends BaseModel {

  private String content;
  @ApiModelProperty("食客")
  private Integer fkDinerId;
  @ApiModelProperty("点赞")
  private int praiseAmount;
  @ApiModelProperty("评论")
  private int commentAmount;
  @ApiModelProperty("关联的餐厅")
  private Integer fkRestaurantId;


}
