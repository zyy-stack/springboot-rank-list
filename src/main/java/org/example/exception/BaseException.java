package org.example.exception;

import lombok.Data;
import org.example.Enum.BaseExceptionEnum;

@Data
public class BaseException extends RuntimeException{
    private Integer status;

    private String message;

    public BaseException(){
        super();
    }


    public BaseException(BaseExceptionEnum baseExceptionEnum){
        super(String.valueOf(baseExceptionEnum.getStatus()));
        this.status=baseExceptionEnum.getStatus();
        this.message=baseExceptionEnum.getMessage();
    }
}
