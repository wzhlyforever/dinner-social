package com.imooc.diners.controller;

import cn.hutool.core.util.RandomUtil;
import com.imooc.commons.constants.PointTypesContant;
import com.imooc.diners.PointsApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-23 19:34
 **/
public class DinerPointControllerTest extends PointsApplicationTest {


  @Test
  public void addPoints () throws Exception{

    for (int i = 1; i < 2000; i++) {
      for (int j = 0; j < 10; j++) {

        super.mockMvc.perform(MockMvcRequestBuilders.post("/")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("dinerId", i + "")
            .param("points", RandomUtil.randomNumbers(2))
            .param("type", PointTypesContant.sign.getType().toString())
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
      }
    }

  }

}
