package com.imooc.commons.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-13 23:04
 **/

@Data
@ApiModel(description = "用户注册信息")
public class DinnerInfoDTO {


  @ApiModelProperty("用户名")
  private String userName;

  @ApiModelProperty("密码")
  private String password;

  @ApiModelProperty("用户手机")
  private String phone;

  @ApiModelProperty("短信验证码")
  private String verifyCode;





}
