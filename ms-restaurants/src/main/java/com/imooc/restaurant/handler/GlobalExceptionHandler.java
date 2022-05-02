package com.imooc.restaurant.handler;

import com.imooc.commons.exception.ParamterException;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestControllerAdvice // 将输出的内容写入 ResponseBody 中
@Slf4j
public class GlobalExceptionHandler {

    @Resource
    private HttpServletRequest request;

    @ExceptionHandler(ParamterException.class)
    public ResultInfo<Map<String, String>> handlerParamterException(ParamterException ex) {
        String path = request.getRequestURI();
        ResultInfo<Map<String, String>> resultInfo =
                ResultInfoUtil.buildError(ex.getErrCode(), ex.getMessage(), path);
        return resultInfo;
    }

    @ExceptionHandler(Exception.class)
    public ResultInfo<Map<String, String>> handlerException(Exception ex) {
        log.info("未知异常：{}", ex);
        String path = request.getRequestURI();
        ResultInfo<Map<String, String>> resultInfo =
                ResultInfoUtil.buildError(path);
        return resultInfo;
    }

}