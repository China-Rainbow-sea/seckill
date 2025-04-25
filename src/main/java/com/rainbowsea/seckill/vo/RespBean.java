package com.rainbowsea.seckill.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用返回前端的信息类
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespBean {

    private long code;

    private String message;

    private Object data;


    /**
     * 成功后-同时携带数据
     *
     * @param data 数据
     * @return RespBean
     */
    public static RespBean success(Object data) {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), data);
    }


    public static RespBean success() {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), null);
    }

    /**
     * 失败各有不同-返回失败信息，不携带数据
     *
     * @param respBeanEnum 失败枚举信息
     * @return RespBean
     */
    public static RespBean error(RespBeanEnum respBeanEnum) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), null);
    }

    /**
     * 失败各有不同-返回失败信息，同时携带数据
     *
     * @param respBeanEnum 失败枚举信息
     * @param data         失败的信息
     * @return RespBean
     */
    public static RespBean error(RespBeanEnum respBeanEnum, Object data) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), data);
    }


}
