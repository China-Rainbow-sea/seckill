package com.rainbowsea.seckill.controller;


import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.service.GoodsService;
import com.rainbowsea.seckill.service.UserService;
import com.rainbowsea.seckill.vo.GoodsVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;


/**
 * 商品列表处理
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {


    @Resource
    private UserService userService;

    @Resource
    private GoodsService goodsService;


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
    public String toList(Model model, User user) {
        // //验证部分的代码，可以注销了，WebMvcConfigurer使用 mvc进行优化,避免每次都要

        if (null == user) { // 用户没有成功登录
            return "login";
        }


        // 将 user 放入到 model,携带该下一个模板使用
        model.addAttribute("user", user);

        //展示商品
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }


    /**
     * 跳转商品详情页面
     *
     * @param model
     * @param user
     * @param goodsId
     * @return String 跳转到对应 templates 下对应的html页面
     */
    @RequestMapping(value = "/toDetail/{goodsId}")
    public String toDetail(Model model, User user, @PathVariable("goodsId") Long goodsId) {
        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        // 说明: 返回秒杀商品详情时，同时返回该商品的秒杀状态和秒杀的剩余时间
        // 为了配合前端展示秒杀前端的状态
        // 1. 变量 secKillStatus 秒杀状态 0表示:秒杀未开始，1:秒杀进行中，2:秒杀已经结束
        // 2. 变量 remainSeconds 剩余秒数: >0表示:还有多久开始秒杀, 0:表示秒杀进行中，-1:表示秒杀结束

        // 秒杀开始时间
        Date startDate = goodsVo.getStartDate();
        // 秒杀结束时间
        Date endDate = goodsVo.getEndDate();
        // 当前时间
        Date nowDate = new Date();

        int secKillStatus = 0;
        int remainSeconds = 0;

        // 如果nowDate 在 startDate 前，说明还没有开始秒杀
        if (nowDate.before(startDate)) {
            //  startDate.getTime() 返回的是毫秒 , 1000 表示 每秒
            secKillStatus = 0;  // 秒杀未开始
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if (nowDate.after(endDate)) {
            secKillStatus = 2; // 表示秒杀已经结束
            remainSeconds = -1; // 表示秒杀已经结束
        } else {
            // 秒杀进行中
            secKillStatus = 1;
            remainSeconds = 0;
        }


        // 将 secKillStatus 和 remainSeconds 放入到 model，携带给下模板页使用
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        model.addAttribute("goods", goodsVo);
        return "goodsDetail";

    }
}
