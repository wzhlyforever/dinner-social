package com.imooc.feed.handler;

import com.imooc.commons.constants.ApiConstant;
import com.imooc.commons.exception.ParamterException;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @program: food-social-contact-parent
 * @description: 自定义异常处理类
 * @author: Mr.Wang
 * @create: 2022-04-14 11:30
 **/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


  @Resource
  private HttpServletRequest httpServletRequest;

  /**
   * 处理自定义的异常
   *
   * @param exception
   * @return
   */
  @ExceptionHandler(value = ParamterException.class)
  public ResultInfo paramterExceptionHandler(ParamterException exception) {

    return ResultInfoUtil.buildError(ApiConstant.ERROR_CODE, exception.getMessage(),
        httpServletRequest.getServletPath());
  }


  /**
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(value = Exception.class)
  public ResultInfo exceptionHandler(Exception ex) {
    log.info("未知异常：{}",
        ex);
    String path = httpServletRequest.getRequestURI();
    ResultInfo<Map<String, String>> resultInfo =
        ResultInfoUtil.buildError(path);
    return resultInfo;
  }
}
