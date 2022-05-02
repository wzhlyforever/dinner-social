package com.imooc.oauth2.server.config;

import com.imooc.commons.model.domain.SignInIdentity;
import com.imooc.oauth2.server.service.UserService;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;


/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-01 18:33
 **/
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {


  @Resource
  private ClientOauth2DataConfiguration clientOauth2DataConfiguration;


  @Resource
  private AuthenticationManager authenticationManager;

  @Resource
  private PasswordEncoder passwordEncoder;

  @Resource
  private RedisTokenStore redisTokenStore;

  @Resource
  private UserService userService;

  /**
   * @param security
   * @throws Exception 用来配置token令牌端点的安全约束
   */
  @Override
  public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
    // /oauth/token     /oauth/check_token  默认是不能被访问的
    security.tokenKeyAccess("permitAll()").checkTokenAccess("permitAll()");
  }

  /*
  client 的配置信息
   */
  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.inMemory()
        .withClient(clientOauth2DataConfiguration.getClientId())
        .secret(passwordEncoder.encode(clientOauth2DataConfiguration.getSecret()))
        .authorizedGrantTypes(clientOauth2DataConfiguration.getGrantTypes())
        .accessTokenValiditySeconds(clientOauth2DataConfiguration.getTokenValidityTime())
        .refreshTokenValiditySeconds(clientOauth2DataConfiguration.getRefreshTokenValidityTime())
        .scopes(clientOauth2DataConfiguration.getScopes());

  }

  /*

   */
  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints.authenticationManager(authenticationManager)
        .userDetailsService(userService)
        .tokenStore(redisTokenStore)
        .tokenEnhancer((token, authentication) -> {
          SignInIdentity signInIdentity = ((SignInIdentity) authentication.getPrincipal());
          DefaultOAuth2AccessToken accessToken = ((DefaultOAuth2AccessToken) token);
          Map<String, Object> additionalInformation = new LinkedHashMap<>();
          additionalInformation.put("nickname", signInIdentity.getNickname());
          additionalInformation.put("avatarUrl", signInIdentity.getAvatarUrl());
          accessToken.setAdditionalInformation(additionalInformation);
          return accessToken;
        });     // 对token进行增强

  }
}
