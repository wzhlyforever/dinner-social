package com.imooc.gateway.filter;

import com.imooc.component.HandleException;
import com.imooc.gateway.config.IgnoreUrlConfig;

import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-03 16:20
 **/
public class AuthGlobalFilter implements GlobalFilter, Ordered {

  @Resource
  private IgnoreUrlConfig ignoreUrlConfig;

  @Resource
  private HandleException handleException;

  @Resource
  private RestTemplate restTemplate;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // 客户端发送请求， 获取路径是否在白名单中，在白名单中就放行
    AntPathMatcher matcher = new AntPathMatcher();
    boolean isRelease = false;

    String path = exchange.getRequest().getURI().getPath();
    for (String url : ignoreUrlConfig.getUrls()) {
      if (matcher.match(url, path)) {
        isRelease = true;
      }
    }
    // 在白名单中，放行
    if (isRelease) {
      return chain.filter(exchange);
    }

    // 不在白名单中则 check token, 获取access_token
    String access_token = exchange.getRequest().getQueryParams().getFirst("access_token");

    // 判断token 是否为空
    if (StringUtils.isBlank(access_token)) {
      return handleException.writeError(exchange, "请登录");
    }

    // 验证token 是否有效
    String checkTokenUrl = "http://ms-oauth2-server/oauth/check_token?token=".concat(access_token);
    try {
      // 发送远程请求，验证 token
      ResponseEntity<String> entity = restTemplate.getForEntity(checkTokenUrl, String.class);
      // token 无效的业务逻辑处理
      if (entity.getStatusCode() != HttpStatus.OK) {
        return handleException.writeError(exchange,
            "Token was not recognised, token: ".concat(access_token));
      }
      if (StringUtils.isBlank(entity.getBody())) {
        return handleException.writeError(exchange,
            "This token is invalid: ".concat(access_token));
      }
    } catch (Exception e) {
      return handleException.writeError(exchange,
          "Token was not recognised, token: ".concat(access_token));
    }

    // 放行
    return chain.filter(exchange);


  }

  @Override
  public int getOrder() {
    return 0;
  }
}
