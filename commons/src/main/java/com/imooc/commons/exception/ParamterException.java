package com.imooc.commons.exception;

import com.imooc.commons.constants.ApiConstant;
import lombok.Getter;
import lombok.Setter;


/**
 * @program: food-social-contact-parent
 * @description: 全局异常类
 * @author: Mr.Wang
 * @create: 2022-02-27 17:35
 **/
@Getter
@Setter
public class ParamterException extends RuntimeException {

  private Integer errCode;   //错误码


  public ParamterException() {
    super(ApiConstant.ERROR_MESSAGE);
    this.errCode = ApiConstant.ERROR_CODE;
  }

  public ParamterException(Integer errCode) {
    this.errCode = errCode;

  }

  public ParamterException(String message) {
    super(message);
    this.errCode = ApiConstant.ERROR_CODE;
  }

  public ParamterException(String message, Integer errCode) {
    super(message);
    this.errCode = errCode;
  }
}
