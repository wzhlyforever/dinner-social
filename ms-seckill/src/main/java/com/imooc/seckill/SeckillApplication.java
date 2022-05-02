package com.imooc.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-14 19:36
 **/
@MapperScan("com.imooc.seckill.mapper")
@SpringBootApplication
public class SeckillApplication {

  public static void main(String[] args) {
    SpringApplication.run(SeckillApplication.class);
  }

}