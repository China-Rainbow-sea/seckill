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
    MOBILE_ERROR(500212, "手机号码格式不正确"),
    MOBILE_NOT_EXIST(500213, "手机号码不存在"),
    PASSWROD_UPDATE_FAIL(500214, "密码更新失败"),

    // 秒杀模块-返回的信息
    ENTRY_STOCK(500500, "库存不足"),
    REPEAT_ERROR(500501, "该商品每人限购一件"),
    REQUEST_ILLEGAL(500502, "请求非法"),
    SESSION_ERROR(500503, "用户信息有误"),
    SKL_KILL_WATT(500504, "排队中..."),
    CAPTCHA_ERROR(500505, "验证码错误"),
    ACCESS_LIMIT_REACHED(500506, "访问频繁，请待会再试"),
    SET_KILL_RETRY(500507, "本次抢购失败，请继续抢购");

    private final Integer code;

    private final String message;
}
