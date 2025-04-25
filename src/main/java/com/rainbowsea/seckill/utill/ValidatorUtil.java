package com.rainbowsea.seckill.utill;


import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 完成一些校验工作，比如手机号码格式是否正确。
 * 提醒: 这里我们使用正则表达式
 */
public class ValidatorUtil {

    // 校验手机号码的正则表达式，这里只是简单的一个校验
    // 13300000000 合格
    // 1330000000000 不合格
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[1][3-9][0-9]{9}$");

    public static boolean isMobile(String mobile) {
        if (!StringUtils.hasText(mobile)) {
            return false;
        }


        // 进行正则表达式校验-Java基础
        Matcher matcher = MOBILE_PATTERN.matcher(mobile);
        return matcher.matches();
    }


    // 测试一下校验方法:
    @Test
    public void t1() {
       String mobile =  "13300000000";
       String mobile2 =  "1330000000000";

        System.out.println(isMobile(mobile));
        System.out.println(isMobile(mobile2));

    }

}
