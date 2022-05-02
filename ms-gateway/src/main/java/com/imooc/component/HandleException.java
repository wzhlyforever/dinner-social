package com.imooc.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-03 18:50
 **/
@Component
public class HandleException {


  @Resource
  private ObjectMapper objectMapper;


  public Mono<Void> writeError(ServerWebExchange exchange, String errMessage) {
    ServerHttpResponse response = exchange.getResponse();
    ServerHttpRequest request = exchange.getRequest();
    response.setStatusCode(HttpStatus.OK);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    ResultInfo result = ResultInfoUtil.buildError(ApiConstant.ERROR_CODE, errMessage, request.getURI().getPath());
    DataBuffer dataBuffer = null;
    try {
      dataBuffer= response.bufferFactory()
          .wrap(objectMapper.writeValueAsString(result).getBytes(
              StandardCharsets.UTF_8));
    }  catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return response.writeWith(Mono.just(dataBuffer));
  }

}
