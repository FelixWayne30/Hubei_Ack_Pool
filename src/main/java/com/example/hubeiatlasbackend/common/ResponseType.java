package com.example.hubeiatlasbackend.common;

import java.io.Serializable;

public enum ResponseType implements Serializable {
    SUCCESS(200,"接口返回正常"),
    FAILURE(5000,"接口返回失败"),
    PARAMS_ERROR(5100,"提交参数错误"),
    FLOW(429,"请求过多限流"),
    DEGRADE(430,"请求过多降级"),
    PARAMS_ERROR_MISS(5101,"必填参数缺失"),
    PARAMS_ERROR_INVALID(5102,"参数无效"),
    NULL_FILE(5103,"文件不能为空"),
    PERMISSION_DENIED(6000,"权限拒绝"),
    DENIED_NOT_ROLE(6100,"角色权限拒绝"),
    DENIED_NOT_ADMIN(6101,"非管理员权限"),
    DENIED_NOT_USER(6102,"用户权限拒绝"),
    ERROR(500,"服务器内部错误"),
    CUSTOM(999,"其他错误"),
    NOT_FOUND(404, "未找到该资源!");
    private Integer code;
    private String msg;

    ResponseType(Integer code, String msg){
        this.code=code;
        this.msg=msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString(){
        return this.code+'-'+this.msg;
    }
}
