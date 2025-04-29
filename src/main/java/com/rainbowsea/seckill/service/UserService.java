package com.rainbowsea.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.vo.LoginVo;
import com.rainbowsea.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author huo
 * @description 针对表【seckill_user】的数据库操作Service
 * @createDate 2025-04-24 15:38:01
 */
public interface UserService extends IService<User> {


    /**
     * 登录校验
     *
     * @param loginVo  登录时发送的信息
     * @param request  request
     * @param response response
     * @return RespBean
     */
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request,
                     HttpServletResponse response);


    /**
     * 根据 Cookie 当中的 userTicket 获取判断，存储到 Redis 当中的用户信息
     * @param userTicket  Cookie 当中的 userTicket
     * @param request
     * @param response
     * @return 存储到 Redis 当中的 User 对象信息
     */
    User getUserByCookieByRedis(String userTicket,
                                HttpServletRequest request,
                                HttpServletResponse response);


    /**
     * 更改密码 ，更新 Redis 当中的用户缓存信息。
     * @param userTicket
     * @param password
     * @param request
     * @param response
     * @return
     */
    RespBean updatePassword
            (String userTicket, String password,
             HttpServletRequest request, HttpServletResponse response);
}
