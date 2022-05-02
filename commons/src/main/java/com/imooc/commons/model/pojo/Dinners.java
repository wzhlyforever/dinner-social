package com.imooc.commons.model.pojo;

import com.imooc.commons.model.base.BaseModel;
import lombok.Data;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-01 19:49
 **/
@Data
public class Dinners extends BaseModel {

  // 主键
  private Integer id;
  // 用户名
  private String username;
  // 昵称
  private String nickname;
  // 密码
  private String password;
  // 手机号
  private String phone;
  // 邮箱
  private String email;
  // 头像
  private String avatarUrl;
  // 角色
  private String roles;
}
