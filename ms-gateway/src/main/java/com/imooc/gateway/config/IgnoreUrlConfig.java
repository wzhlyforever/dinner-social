package com.imooc.gateway.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-03 16:35
 **/
@Configuration
@ConfigurationProperties(prefix = "secure.ignore")
@Data
public class IgnoreUrlConfig {


  private List<String> urls;

}
