package com.imooc.oauth2.server.controller;

import cn.hutool.core.util.StrUtil;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.domain.SignInIdentity;
import com.imooc.commons.model.vo.SignInDinerInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: food-social-contact-parent
 * @description: 用户中心  退出登录功能
 * @author: Mr.Wang
 * @create: 2022-04-02 22:01
 **/

@RestController
public class UserController {


  @Resource
  private RedisTokenStore redisTokenStore;

  @Resource
  private HttpServletRequest request;

  @GetMapping("user/me")
  public ResultInfo userme(Authentication authentication) {
    SignInIdentity signInIdentity = ((SignInIdentity) authentication.getPrincipal());
    SignInDinerInfo signInDinerInfo = new SignInDinerInfo();
    BeanUtils.copyProperties(signInIdentity, signInDinerInfo);
    return ResultInfoUtil.buildSuccess(request.getServletPath(), signInDinerInfo);
  }


  @GetMapping("user/logout")
  public ResultInfo logOut(String access_token, @RequestHeader("Authorization")String authorization) {
    if (StringUtils.isBlank(access_token)) {
      access_token = authorization;
    }
    System.out.println("access_token = " + access_token);
    if (StringUtils.isBlank(access_token)) {
      return ResultInfoUtil.buildSuccess(request.getServletPath(), "log out");
    }

    if (access_token.toLowerCase().contains("bearer ".toLowerCase())) {
      access_token = access_token.toLowerCase().replace("bearer ", "");
    }

    OAuth2AccessToken oAuth2AccessToken = redisTokenStore.readAccessToken(access_token);
    if (oAuth2AccessToken != null) {
      redisTokenStore.removeAccessToken(oAuth2AccessToken);
      OAuth2RefreshToken refreshToken = oAuth2AccessToken.getRefreshToken();
      redisTokenStore.removeRefreshToken(refreshToken);
    }

    return ResultInfoUtil.buildSuccess(request.getServletPath(), "退出成功");


  }

}
