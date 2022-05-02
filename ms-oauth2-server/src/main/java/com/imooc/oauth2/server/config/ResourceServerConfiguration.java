package com.imooc.oauth2.server.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-02 23:32
 **/
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {


  @Resource
  private MyAuthenticationEntryPoint myAuthenticationEntryPoint;

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .anyRequest()
        .authenticated()
        .and()
        .requestMatchers()
        .antMatchers("/user/**");
  }


  @Override
  public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
    resources.authenticationEntryPoint(myAuthenticationEntryPoint);
  }
}
