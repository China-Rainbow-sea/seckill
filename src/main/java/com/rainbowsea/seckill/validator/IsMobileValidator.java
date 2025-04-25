package com.rainbowsea.seckill.validator;


import com.rainbowsea.seckill.utill.ValidatorUtil;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 我们自拟定注解 IsMobile(手机号是否正确) 的校验规则
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        // 初始化
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        //必填
        if (required) {
            return ValidatorUtil.isMobile(value);
        } else {//非必填
            if (!StringUtils.hasText(value)) {
                return true;
            } else {
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
