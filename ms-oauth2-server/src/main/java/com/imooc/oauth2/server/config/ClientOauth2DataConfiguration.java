package com.imooc.oauth2.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-01 18:44
 **/

@Configuration
@ConfigurationProperties(prefix = "client.oauth2")
@Data
public class ClientOauth2DataConfiguration {

  // 客户端标识 ID
  private String clientId;

  // 客户端安全码
  private String secret;

  // 授权类型
  private String[] grantTypes;

  // token有效期
  private int tokenValidityTime;

  // refresh-token有效期
  private int refreshTokenValidityTime;

  // 客户端访问范围
  private String[] scopes;

}
