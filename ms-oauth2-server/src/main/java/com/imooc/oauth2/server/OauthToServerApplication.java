package com.imooc.oauth2.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-01 17:14
 **/
@MapperScan("com.imooc.oauth2.server.mapper")
@SpringBootApplication
public class OauthToServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(OauthToServerApplication.class, args);
  }

}
