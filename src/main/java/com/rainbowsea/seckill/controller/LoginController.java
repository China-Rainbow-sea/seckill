package com.rainbowsea.seckill.controller;


import com.rainbowsea.seckill.service.UserService;
import com.rainbowsea.seckill.vo.LoginVo;
import com.rainbowsea.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {


    @Resource
    private UserService userService;


    /**
     * 用户登录
     *
     * @return 返回登录页面
     */
    @RequestMapping("/toLogin")
    public String toLogin() {

        return "login"; // 到templates/login.html
    }

    /**
     * 登录功能
     */
    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin
    (@Valid LoginVo loginVo, HttpServletRequest request,
     HttpServletResponse response) {
        log.info("{}", loginVo);
        return userService.doLogin(loginVo, request, response);
    }


    
}


