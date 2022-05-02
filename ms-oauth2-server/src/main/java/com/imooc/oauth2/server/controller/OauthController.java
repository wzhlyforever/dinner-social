package com.imooc.oauth2.server.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-01 22:44
 **/

@RestController
@RequestMapping("oauth")
public class OauthController {


  @Resource
  private TokenEndpoint tokenEndpoint;


  @Resource
  private HttpServletRequest httpServletRequest;



  @PostMapping("token")
  public ResultInfo postAccesstoken(Principal principal, @RequestParam
      Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
    return custom(tokenEndpoint.postAccessToken(principal, parameters).getBody());
  }

  private ResultInfo custom(OAuth2AccessToken oAuth2AccessToken) {
    DefaultOAuth2AccessToken defaultOAuth2AccessToken = ((DefaultOAuth2AccessToken) oAuth2AccessToken);   // 增强之后的token
    Map<String, Object> data = new LinkedHashMap<>(
        defaultOAuth2AccessToken.getAdditionalInformation());
    data.put("accessToken", defaultOAuth2AccessToken.getValue());
    data.put("expireIn", defaultOAuth2AccessToken.getExpiresIn());
    data.put("scopes", defaultOAuth2AccessToken.getScope());
    if (defaultOAuth2AccessToken.getRefreshToken() != null) {
      data.put("refresh_token", defaultOAuth2AccessToken.getRefreshToken().getValue());
    }

    return ResultInfoUtil.buildSuccess(httpServletRequest.getServletPath(), data);
  }


}
