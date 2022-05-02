package com.imooc.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.dto.DinnerInfoDTO;
import com.imooc.commons.model.pojo.Dinners;
import com.imooc.commons.model.vo.ShortInDinerInfo;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.diners.config.Oauth2ClientConfiguration;
import com.imooc.diners.domain.OAuthDinerInfo;
import com.imooc.diners.mapper.DinersMapper;
import com.imooc.diners.vo.LoginDinerInfo;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @program: food-social-contact-parent
 * @description: 食客登录逻辑
 * @author: Mr.Wang
 * @create: 2022-04-02 17:23
 **/
@Service
public class DinnerService {


  @Resource
  private RestTemplate restTemplate;

  @Resource
  private Oauth2ClientConfiguration clientConfiguration;

  @Value("${service.name.ms-oauth-server}")
  private String oauthServerName;

  @Resource
  private SendVerifyCodeService sendVerifyCodeService;


  @Resource
  private DinersMapper dinersMapper;



  /**
   * 根据 ids 查询食客信息
   *
   * @param ids 主键 id，多个以逗号分隔，逗号之间不用空格
   * @return
   */
  public List<ShortInDinerInfo> findByIds(String ids) {
    AssertUtil.isNotEmpty(ids);
    String[] idArr = ids.split(",");
    List<ShortInDinerInfo> dinerInfos = dinersMapper.findByIds(idArr);
    return dinerInfos;
  }



  /**
   * 注册完了之后，走登录逻辑
   * @param dinnerInfoDTO
   * @param path
   * @return
   */
  public ResultInfo register(DinnerInfoDTO dinnerInfoDTO, String path) {

    // 对dto作健壮性判断
    String username = dinnerInfoDTO.getUserName();
    AssertUtil.isNotEmpty(username, "用户名不能为空");
    String password = dinnerInfoDTO.getPassword();
    AssertUtil.isNotEmpty(password, "密码不能为空");
    String phone = dinnerInfoDTO.getPhone();
    AssertUtil.isNotEmpty(phone, "手机号不能为空");
    String code = dinnerInfoDTO.getVerifyCode();
    AssertUtil.isNotEmpty(code, "请输入验证码啊");


    // 获取验证码
    String sms_code = sendVerifyCodeService.retrieveVerifyCode(phone);
    AssertUtil.isNotEmpty(sms_code, "验证码到期, 请重新生成验证码");

    // 验证码校验
    AssertUtil.isTrue(!code.equals(sms_code), "两次验证码不相同，请重新输入");

    // 判断用户是否已经注册
    Dinners dinners = dinersMapper.selectByUsername(username.trim());
    AssertUtil.isTrue(dinners != null, "该用户已经注册");

    // 用户注册
    dinnerInfoDTO.setPassword(DigestUtil.md5Hex(password.trim()));

    dinersMapper.save(dinnerInfoDTO);
    return signIn(username.trim(), password.trim(), path);

  }

  /**
   * 校验手机号是否已注册
   */
  public void checkPhoneIsRegistered(String phone) {
    AssertUtil.isNotEmpty(phone, "手机号不能为空");
    Dinners diners = dinersMapper.selectByPhone(phone);
    AssertUtil.isTrue(diners == null, "该手机号未注册");
    AssertUtil.isTrue(diners.getIsValid() == 0, "该用户已锁定，请先解锁");
  }


  public ResultInfo signIn(String account, String password, String path) {
    // 判断是否为空
    AssertUtil.isNotEmpty(account, "账号不能为空");
    AssertUtil.isNotEmpty(password, "密码不能为空");

    //  构造请求头
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    //   构造请求体
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("username", account);
    body.add("password", password);
    body.setAll(BeanUtil.beanToMap(clientConfiguration));
    HttpEntity<MultiValueMap<String,Object>> httpEntity = new HttpEntity<>(body, headers);
    // 设置Authentication
    restTemplate.getInterceptors()
        .add(new BasicAuthenticationInterceptor(clientConfiguration.getClientId(),
            clientConfiguration.getSecret()));
    //  构造请求
    ResponseEntity<ResultInfo> result = restTemplate
        .postForEntity(oauthServerName + "oauth/token",
            httpEntity, ResultInfo.class);

    // 处理结果
    AssertUtil.isTrue(result.getStatusCode() != HttpStatus.OK, "登录失败");
    ResultInfo resultInfo = result.getBody();

    if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
      resultInfo.setData(resultInfo.getMessage());
      return resultInfo;
    }

    OAuthDinerInfo oAuthDinerInfo = BeanUtil.fillBeanWithMap(
        ((LinkedHashMap) resultInfo.getData()),
        new OAuthDinerInfo(), false
    );

    // 返回视图对象
    LoginDinerInfo loginDinerInfo = new LoginDinerInfo();
    loginDinerInfo.setAvatarUrl(oAuthDinerInfo.getAvatarUrl());
    loginDinerInfo.setNickname(oAuthDinerInfo.getAvatarUrl());
    loginDinerInfo.setToken(oAuthDinerInfo.getAccessToken());
    return ResultInfoUtil.buildSuccess(path, loginDinerInfo);
  }


}
