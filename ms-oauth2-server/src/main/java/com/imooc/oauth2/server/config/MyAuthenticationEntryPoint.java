package com.imooc.oauth2.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-03 14:29
 **/
@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Resource
  private ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {

    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    String errMessage = authException.getMessage();
    if (StringUtils.isBlank(errMessage)) {
      errMessage = "登录失败";
    }
    ResultInfo<Object> result = ResultInfoUtil
        .buildError(ApiConstant.ERROR_CODE, errMessage,
            request.getServletPath());
    final PrintWriter out = response.getWriter();

    out.write(objectMapper.writeValueAsString(result));
    out.flush();
    out.close();
  }
}
