package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.common.ResponseResult;
import com.example.hubeiatlasbackend.common.ResponseType;
import org.springframework.http.HttpStatus;

public abstract class BaseController {
    public Object renderSuccess(){
        return new ResponseResult<>(HttpStatus.OK.value(), ResponseType.SUCCESS.getMsg());
    }

    public Object renderSuccess(String msg, Object data) {
        return new ResponseResult<>(HttpStatus.OK.value(), msg, data);
    }

    public Object renderSuccess(Object data) {
        return new ResponseResult<>(HttpStatus.OK.value(), ResponseType.SUCCESS.getMsg(), data);
    }

    public Object renderSuccess(String msg, Object[] data) {
        int size = 0;
        if (data != null) {
            size = data.length;
        }
        return new ResponseResult<>(HttpStatus.OK.value(), msg, data, size);
    }

    public Object renderSuccess(String msg) {
        return renderSuccess(msg, null);
    }

    public Object renderError(String failure) {
        return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), failure);
    }

    public Object renderError(int code, String failure) {
        return new ResponseResult<>(code, failure);
    }

    public Object renderFlow(Object flow) {
        return new ResponseResult<>(HttpStatus.TOO_MANY_REQUESTS.value(), ResponseType.FLOW.getMsg(), flow);
    }

    public Object renderDegrade(Object degrade) {
        return new ResponseResult<>(ResponseType.DEGRADE.getCode(), ResponseType.DEGRADE.getMsg(), degrade);
    }
}
