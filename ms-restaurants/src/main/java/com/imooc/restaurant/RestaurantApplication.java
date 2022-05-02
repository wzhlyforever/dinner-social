package com.imooc.restaurant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-25 18:56
 **/

@MapperScan("com.imooc.restaurant.mapper")
@SpringBootApplication
public class RestaurantApplication {

  public static void main(String[] args) {
    SpringApplication.run(RestaurantApplication.class, args);

  }

}
