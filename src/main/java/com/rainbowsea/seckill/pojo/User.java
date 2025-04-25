package com.rainbowsea.seckill.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName seckill_user
 */
@TableName(value = "seckill_user")
@Data
public class User implements Serializable {


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


    /**
     * 用户 ID, 设为主键, 唯一 手机号
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     *
     */
    private String nickname;

    /**
     * MD5(MD5(pass 明 文 + 固 定
     * salt)+salt)
     */
    private String password;

    /**
     *
     */
    private String slat;

    /**
     * 头像
     */
    private String head;

    /**
     * 注册时间
     */
    private Date registerDate;

    /**
     * 最后一次登录时间
     */
    private Date lastLoginDate;

    /**
     * 登录次数
     */
    private Integer loginCount;

}