package com.imooc.diners;

import javax.annotation.Resource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-23 19:12
 **/

@SpringBootTest
@AutoConfigureMockMvc
public class PointsApplicationTest {


  @Resource
  public MockMvc mockMvc;

}
