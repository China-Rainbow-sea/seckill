package com.rainbowsea.seckill.validator;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 开发一个自定义的注解：替换如下，登录校验时的代码
 * <p>
 * <p>
 * // 判断手机号/id，和密码是否为空
 * if (!StringUtils.hasText(mobile) || !StringUtils.hasText(password)) {
 * return RespBean.error(RespBeanEnum.LOGIN_ERROR);
 * }
 * <p>
 * // 判断手机号是否合格
 * if (!ValidatorUtil.isMobile(mobile)) {
 * return RespBean.error(RespBeanEnum.MOBILE_ERROR);
 * }
 */

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER,
        TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidator.class})
public @interface IsMobile {

    String message() default "手机号码格式错误";

    boolean required() default true;

    Class<?>[] groups() default {}; // 默认参数

    Class<? extends Payload>[] payload() default {}; //默认参数
}
