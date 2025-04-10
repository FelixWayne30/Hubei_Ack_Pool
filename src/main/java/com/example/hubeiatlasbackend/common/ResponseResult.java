package com.example.hubeiatlasbackend.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Data
public class ResponseResult<T> implements Serializable {
    private static final long serialVersionUID = -2395642098714985649L;

    @JsonIgnore
    private ResponseType resultType;

    private Integer code;

    private String msg;

    private T data;

    private long size;

    public ResponseResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResponseResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResponseResult(int code, String msg, T data, int size) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.size = size;
    }

    public ResponseResult() {
    }

    public static <T> ResponseResult<T> success(String message, T data) throws Exception {
        return new ResponseResult<>(HttpStatus.OK.value(), message, data);
    }

    public static <T> ResponseResult<T> success(String message) throws Exception {
        return success(message, null);
    }

    public static <T> ResponseResult<T> error(String message) {
        return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null);
    }

    public static <T> ResponseResult<T> error(int code, String message) {
        return new ResponseResult<>(code, message, null);
    }

    public Object renderParamsError(String error) {
        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), error);
    }

    public static ResponseResult<String> renderFlow(String flow) {
        return new ResponseResult<>(HttpStatus.TOO_MANY_REQUESTS.value(), ResponseType.FLOW.getMsg(), flow);
    }

    public static ResponseResult<String> renderDegrade(String degrade) {
        return new ResponseResult<>(ResponseType.DEGRADE.getCode(), ResponseType.DEGRADE.getMsg(), degrade);
    }
}
