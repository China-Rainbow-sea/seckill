package com.rainbowsea.seckill.utill;

import org.apache.commons.codec.digest.DigestUtils;


/**
 * MD5 加密工具类,根据前面密码设计方案提供相应的方法
 */
public class MD5Util {


    /**
     * 第一次加密所需的盐。模拟前端用户加的盐。
     */
    private static final String SALT = "UCmP7xHA";


    /**
     * MD5 加密
     *
     * @param src 要加密的字符串
     * @return String
     */
    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }


    /**
     * 加密加盐,完成的是 md5（pass+salt1）
     *
     * @param inputPass 输入的密码
     * @return String
     */
    public static String inputPassToMidPass(String inputPass) {
        String str = "" + SALT.charAt(0) + inputPass + SALT.charAt(6);
        return md5(str);
    }


    /**
     * 这个盐随机生成,成的是 md5（ md5（pass+salt1）+salt2）
     *
     * @param midPass 加密的密码
     * @param salt    从数据库获取到不同用户加密的盐
     * @return String
     */
    public static String midPassToDBPass(String midPass, String salt) {
        String str = salt.charAt(1) + midPass + salt.charAt(5);
        return md5(str);
    }


    /**
     * 进行两次加密加盐 最后存到数据库的 md5（ md5（pass+salt1）+salt2）
     * salt1是前端进行的salt2 是后端进行的随机生成
     */
    public static String inputPassToDBPass(String inputPass, String salt) {
        String midPass = inputPassToMidPass(inputPass);
        String dbPass = midPassToDBPass(midPass, salt);
        return dbPass;
    }
}
