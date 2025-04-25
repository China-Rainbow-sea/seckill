package com.rainbowsea.seckill.utill;

import java.util.UUID;


/**
 * 用户生产唯一的 UUID ，作为 session
 */
public class UUIDUtil {

    public static String uuid() {
        // 默认下: 生成的字符串形式 xxxx-yyyy-zzz-ddd
        // 把 UUID中的-替换掉,所以使用 replace("-", "")
        return UUID.randomUUID().toString().replace("-", "");
    }
}
