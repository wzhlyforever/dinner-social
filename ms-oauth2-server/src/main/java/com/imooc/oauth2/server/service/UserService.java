package com.imooc.oauth2.server.service;

import com.imooc.commons.model.domain.SignInIdentity;
import com.imooc.commons.model.pojo.Dinners;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.oauth2.server.mapper.DinersMapper;
import javax.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-01 19:19
 **/

@Service
public class UserService implements UserDetailsService {


  @Resource
  private DinersMapper dinersMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    AssertUtil.isNotEmpty(username,"请输入用户名");
    Dinners dinners = dinersMapper.selectByAccountInfo(username);
    if (dinners == null) {
      throw new UsernameNotFoundException("用户名或密码错误，请重新输入");
    }
    SignInIdentity signInIdentity = new SignInIdentity();
     // 复制属性
    BeanUtils.copyProperties(dinners,signInIdentity);
    return signInIdentity;

//    return new User(username, dinners.getPassword(),
//        AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
  }
}
