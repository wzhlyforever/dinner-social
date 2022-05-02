package com.imooc.commons.model.domain;

import cn.hutool.core.util.StrUtil;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @program: food-social-contact-parent
 * @description: 自定义登录认证对象
 * @author: Mr.Wang
 * @create: 2022-04-02 15:07
 **/

@Getter
@Setter
public class SignInIdentity implements UserDetails {

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

  private int isValid;

  private List<GrantedAuthority> grantedAuthorities;


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (StrUtil.isNotBlank(this.roles)) {
      this.grantedAuthorities = Stream.of(this.roles.split(","))
          .map(role -> {
            return new SimpleGrantedAuthority(role);
          }).collect(Collectors.toList());

    } else {
      this.grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");
    }
    return grantedAuthorities;
  }

  @Override
  public String getPassword() {
    return this.password;
  }


  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return isValid == 1;
  }
}
