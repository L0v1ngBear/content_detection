package org.clf.springboot.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.clf.springboot.common.Result;
import org.clf.springboot.common.enums.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Result> handleCustomException(CustomException e, HttpServletRequest request) {
        ResultCodeEnum resultCode = e.getResultCode();
        logger.error("业务异常：{}，请求路径：{}", resultCode.getMsg(), request.getRequestURI(), e);

        // 1. 用Result构建统一返回体
        Result result = Result.error(resultCode);
        // 2. 设置正确的HTTP状态码（从枚举中获取）
        return new ResponseEntity<>(result, resultCode.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        logger.error(e.getMessage(), e);
        return Result.error(500, "系统异常");
    }
}
