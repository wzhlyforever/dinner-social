package com.imooc.oauth2.server.config;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

/**
 * @program: food-social-contact-parent
 * @description: security配置类
 * @author: Mr.Wang
 * @create: 2022-04-01 17:17
 **/
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private RedisConnectionFactory redisConnectionFactory;

  @Bean
  public RedisTokenStore getredisTokenStore() {
    RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
    // 设置token前缀方便查询
    redisTokenStore.setPrefix("Token: ");
    return redisTokenStore;
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder() {
      @Override
      public String encode(CharSequence rawPassword) {
        return DigestUtil.md5Hex(rawPassword.toString());
      }

      @Override
      public boolean matches(CharSequence rawPassword, String encodePassword) {
        return DigestUtil.md5Hex(rawPassword.toString()).equals(encodePassword);
      }
    };

  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }


  @Override
  protected void configure(HttpSecurity http) throws Exception {
    //放行一些规则
    http.csrf().disable()
        .authorizeRequests()
        .antMatchers("/oauth/**", "actuator/**")
        .permitAll()
        .and()
        .authorizeRequests().anyRequest().authenticated();
  }
}
