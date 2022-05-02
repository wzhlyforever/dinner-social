package com.imooc.diners.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-02 18:23
 **/
@Configuration
@ConfigurationProperties(prefix = "oauth2.client")
@Data
public class Oauth2ClientConfiguration {

  private String clientId;
  private String secret;
  private String grant_type;
  private String scope;


}
