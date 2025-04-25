package com.rainbowsea.seckill.controller;


import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * 商品列表处理
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {


    @Resource
    private UserService userService;


    // 跳转到商品列表页
    //@RequestMapping(value = "/toList")
    //public String toList(HttpSession session,
    //                     Model model,
    //                     @CookieValue("userTicket") String ticket,
    //                     ) {
    /*@RequestMapping(value = "/toList")
    public String toList(Model model,
                         @CookieValue("userTicket") String ticket,
                         HttpServletRequest request,
                         HttpServletResponse response
    ) {
        //  @CookieValue("userTicket") String ticket 注解可以直接获取到,对应 "userTicket" 名称
        // 的cookievalue 信息
        if (!StringUtils.hasText(ticket)) {
            return "login";
        }


        // 通过 cookieVale 当中的 ticket 获取 session 中存放的 user
        //User user = (User) session.getAttribute(ticket);

        // 改为从 Redis 当中获取
        User user = userService.getUserByCookieByRedis(ticket, request, response);

        if (null == user) { // 用户没有成功登录
            return "login";
        }


        // 将 user 放入到 model,携带该下一个模板使用
        model.addAttribute("user", user);

        return "goodsList";
    }*/

    @RequestMapping(value = "/toList")
    public String toList(Model model,User user) {
        // //验证部分的代码，可以注销了，WebMvcConfigurer使用 mvc进行优化,避免每次都要

        if (null == user) { // 用户没有成功登录
            return "login";
        }


        // 将 user 放入到 model,携带该下一个模板使用
        model.addAttribute("user", user);

        return "goodsList";
    }
}
