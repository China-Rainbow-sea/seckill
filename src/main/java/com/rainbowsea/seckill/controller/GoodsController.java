package com.rainbowsea.seckill.controller;


import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.service.GoodsService;
import com.rainbowsea.seckill.service.UserService;
import com.rainbowsea.seckill.vo.GoodsVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;


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


    // Redis 渲染
    @Resource
    private RedisTemplate redisTemplate;


    // Thymeleaf 手动渲染
    @Resource
    private ThymeleafViewResolver thymeleafViewResolver;


    // 跳转到商品列表页 ，没有使用 Redis 缓存的页面
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


    /**
     * 跳转到商品列表页 ，使用上 Redis 缓存的页面
     *
     * @param model
     * @param user     用户
     * @param request
     * @param response
     * @return html 返回的是一个页面对象
     */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody  // 使用了 Redis 缓存页面需要添加
    public String toList(Model model, User user,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        // //验证部分的代码，可以注销了，WebMvcConfigurer使用 mvc进行优化,避免每次都要


        // 先从 Redis 中获取页面，如果不为空，直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        // html 不为空，说明从 Redis 当中获取到了内容，不需要从DB当中获取
        if (StringUtils.hasText(html)) {
            return html;
        }

        if (null == user) { // 用户没有成功登录
            return "login";
        }


        // 将 user 放入到 model,携带该下一个模板使用
        model.addAttribute("user", user);

        //展示商品
        model.addAttribute("goodsList", goodsService.findGoodsVo());

        // 如果为从 Redis 中取出页面为 null，则手动渲染，存入到 Redis 中
        WebContext webContext = new WebContext(request, response,
                request.getServletContext(),
                request.getLocale(), model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);

        if (StringUtils.hasText(html)) { // html 不为空，进入 if ，说明从页面当中获取的页面，存入到 Redis 当中

            // 表示: 每 60s 更新一次 Redis 页面缓存，即60s后，该页面缓存失效，Redis会清除页面缓存。
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
            // 注意：我们这里的这个 goodsList 不可以随便写，而是同我们本身项目当中的 /templates 目录下存在的页面当中映射导入的
        }
        //return "goodsList";

        return html;
    }


    /**
     * 跳转商品详情页面,没有使用 Redis 缓存
     *
     * @param model
     * @param user
     * @param goodsId
     * @return String 跳转到对应 templates 下对应的html页面
     */
    //@RequestMapping(value = "/toDetail/{goodsId}")
    //public String toDetail(Model model, User user, @PathVariable("goodsId") Long goodsId) {
    //    model.addAttribute("user", user);
    //    GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //    // 说明: 返回秒杀商品详情时，同时返回该商品的秒杀状态和秒杀的剩余时间
    //    // 为了配合前端展示秒杀前端的状态
    //    // 1. 变量 secKillStatus 秒杀状态 0表示:秒杀未开始，1:秒杀进行中，2:秒杀已经结束
    //    // 2. 变量 remainSeconds 剩余秒数: >0表示:还有多久开始秒杀, 0:表示秒杀进行中，-1:表示秒杀结束
    //
    //    // 秒杀开始时间
    //    Date startDate = goodsVo.getStartDate();
    //    // 秒杀结束时间
    //    Date endDate = goodsVo.getEndDate();
    //    // 当前时间
    //    Date nowDate = new Date();
    //
    //    int secKillStatus = 0;
    //    int remainSeconds = 0;
    //
    //    // 如果nowDate 在 startDate 前，说明还没有开始秒杀
    //    if (nowDate.before(startDate)) {
    //        //  startDate.getTime() 返回的是毫秒 , 1000 表示 每秒
    //        secKillStatus = 0;  // 秒杀未开始
    //        remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
    //    } else if (nowDate.after(endDate)) {
    //        secKillStatus = 2; // 表示秒杀已经结束
    //        remainSeconds = -1; // 表示秒杀已经结束
    //    } else {
    //        // 秒杀进行中
    //        secKillStatus = 1;
    //        remainSeconds = 0;
    //    }
    //
    //
    //    // 将 secKillStatus 和 remainSeconds 放入到 model，携带给下模板页使用
    //    model.addAttribute("secKillStatus", secKillStatus);
    //    model.addAttribute("remainSeconds", remainSeconds);
    //
    //    model.addAttribute("goods", goodsVo);
    //    return "goodsDetail";
    //
    //}


    /**
     * 跳转商品详情页面,使用 上 Redis 缓存
     *
     * @param model
     * @param user
     * @param goodsId
     * @return String 跳转到对应 templates 下对应的html页面
     */
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody  // 注意：这里需要返回一个我们需要的 html 进行一个解析的渲染。所以这个注解必须要有
    public String toDetail(Model model, User user,
                           @PathVariable("goodsId") Long goodsId,
                           HttpServletResponse response,
                           HttpServletRequest request) {


        //使用页面缓存
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        if (StringUtils.hasText(html)) {
            return html;
        }


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


        // 如果为从 Redis 中取出页面为 null，则手动渲染，存入到 Redis 中
        WebContext webContext = new WebContext(request, response,
                request.getServletContext(),
                request.getLocale(), model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);

        if (StringUtils.hasText(html)) { // html 不为空，进入 if ，说明从页面当中获取的页面，存入到 Redis 当中

            // 表示: 每 60s 更新一次 Redis 页面缓存，即60s后，该页面缓存失效，Redis会清除页面缓存。
            valueOperations.set("goodsDetail", html, 60, TimeUnit.SECONDS);
            // 注意：我们这里的这个 goodsList 不可以随便写，而是同我们本身项目当中的 /templates 目录下存在的页面当中映射导入的
        }


        return html;
        //return "goodsDetail";

    }
}
