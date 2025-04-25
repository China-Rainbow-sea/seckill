package com.rainbowsea.seckill.test;

import com.rainbowsea.seckill.utill.MD5Util;
import org.junit.jupiter.api.Test;

public class MD5UtilTest {


    /**
     * 密码明文 "123456"
     * 1.获取到密码明文"123456" 的中间密码【即客户端加密加盐后的】
     * 2.即第一次加密加盐处理
     * 3. 这个加密加盐的工作，会在客户端/浏览器完成
     */
    @Test
    public void MD5Test() {
        System.out.println(MD5Util.inputPassToMidPass("123456"));
        System.out.println(MD5Util.midPassToDBPass("fa5f296a48eac112a7f9564f736befe4", "cLo8QmTG"));
        System.out.println(MD5Util.inputPassToDBPass("123456", "cLo8QmTG"));

    }
}
