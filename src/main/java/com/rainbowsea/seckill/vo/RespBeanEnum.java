package com.rainbowsea.seckill.vo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


/**
 * 响应的信息枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {

    // 通用
    SUCCESS(200, "SUCCESS"),
    ERROR(500, "服务端异常"),

    // 登录
    LOGIN_ERROR(500210, "用户id 或 密码错误"),
    BING_ERROR(500211, "参数绑定异常~~"),
    MOBILE_ERROR(500211, "手机号码格式不正确"),
    MOBILE_NOT_EXIST(500213, "手机号码不存在");


    private final Integer code;

    private final String message;
}
