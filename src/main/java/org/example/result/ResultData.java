package org.example.result;

import lombok.Data;
import org.example.Enum.BaseExceptionEnum;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
public class ResultData<T> {
    /** 结果状态 ,具体状态码参见ResultData.java*/
    private int status;
    private String message;
    private T data;
    private long timestamp ;


    public ResultData (){
        this.timestamp = System.currentTimeMillis();
    }
    public static <T> ResultData<T> success() {
        ResultData<T> resultData = new ResultData<>();
        resultData.setStatus(BaseExceptionEnum.SUCCESS.getStatus());
        resultData.setMessage(BaseExceptionEnum.SUCCESS.getMessage());
        return resultData;
    }

    public static <T> ResultData<T> success(T data) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setStatus(BaseExceptionEnum.SUCCESS.getStatus());
        resultData.setMessage(BaseExceptionEnum.SUCCESS.getMessage());
        resultData.setData(data);
        return resultData;
    }

    public static <T> ResultData<T> fail(int status, String message) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setStatus(status);
        resultData.setMessage(message);
        return resultData;
    }


    public static <T> ResultData<T> fail(BaseExceptionEnum baseExceptionEnum) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setStatus(baseExceptionEnum.getStatus());
        resultData.setMessage(baseExceptionEnum.getMessage());
        return resultData;
    }

}
