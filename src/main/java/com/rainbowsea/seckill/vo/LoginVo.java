package com.rainbowsea.seckill.vo;


import com.rainbowsea.seckill.validator.IsMobile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * 接收用户登录时，发送的信息(mobile,password)
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVo {

    // 添加 validation 组件后使用
    @NotNull
    @IsMobile  //自拟定注解
    private String mobile;


    @Length(min = 32)
    @NotNull
    private String password;
}
