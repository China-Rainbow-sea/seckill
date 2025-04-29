package com.rainbowsea.seckill.controller;


import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.service.UserService;
import com.rainbowsea.seckill.vo.RespBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 用户登录，返回用户信息
 * <p>
 * 返回用户信息,同时我们也演示如何携带参数
 */
@Controller
@RequestMapping("/user")
public class UserController {


    @Resource
    private UserService userService;

    /**
     * 返回用户信息,同时我们也演示如何携带参数
     *
     * @param user    用户信息，这里的 User对象，我们是通过使用我们上面配置的一个 UserArgumentResolver 自定义自定义参数解析器获取 User 对象
     * @param address 地址
     * @return RespBean
     */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user, String address) {
        return RespBean.success(user);
    }

    @RequestMapping("/updpwd")
    @ResponseBody
    public RespBean updatePassword(String userTicket,
                                   String password,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        return userService.updatePassword(userTicket, password, request, response);

    }
}
