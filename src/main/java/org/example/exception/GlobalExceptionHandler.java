package org.example.exception;

import org.example.Enum.BaseExceptionEnum;
import org.example.result.ResultData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //处理自定义异常
    @ExceptionHandler(value = BaseException.class)
    public ResultData<Object> baseExceptionHandler(BaseException e){
        return ResultData.fail(e.getStatus(),e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResultData handleMissingParams(MissingServletRequestParameterException ex) {
        String parameterName = ex.getParameterName();
        return ResultData.fail(400, "缺少必要参数：" + parameterName);
    }


    //处理其他异常
    @ExceptionHandler(value = Exception.class)
    public  ResultData<Object> exceptionHandler(Exception e){
        return ResultData.fail(BaseExceptionEnum.INTERNAL_SERVER_ERROR);
    }

}