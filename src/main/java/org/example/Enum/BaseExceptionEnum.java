package org.example.Enum;


import lombok.Getter;

@Getter
public enum BaseExceptionEnum implements BaseExceptionInfoInterface{

    SUCCESS(200,"成功！"),
    BODY_NOT_MATCH(400,"数据格式不匹配"),
    INTERNAL_SERVER_ERROR(500,"服务器内部错误！"),
    OPERATION_TOO_FREQUENT(429, "操作过于频繁，请稍后再试"),
    USER_NOT_EXISTS(10001,"当前用户不存在！");



    private Integer status;

    private String message;

    BaseExceptionEnum(Integer status,String message){
        this.status=status;
        this.message=message;
    }


}
