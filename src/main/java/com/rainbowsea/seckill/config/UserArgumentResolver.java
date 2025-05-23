package com.rainbowsea.seckill.config;


import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.service.UserService;
import com.rainbowsea.seckill.utill.CookieUtil;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Rainbowsea
 * @version 1.0
 * UserArgumentResolver: 自定义的一个解析器
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    //装配 UserService
    @Resource
    private UserService userService;

    //判断你当前要解析的参数类型是不是你需要的?
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //获取参数是不是user类型
        Class<?> aClass = parameter.getParameterType();
        //如果为t, 就执行resolveArgument

        // 如果其他的 对象类，可以使用 if 语句进行一个判断
        return aClass == User.class;
    }


    // 修改User 对象直接从 UserContext.getUser() 当前获取即可。
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        // 下面计数器优化的时候通过拦截器获取了 user，存入了 threadlocal，
        // 直接从 threadlocal里拿
        return UserContext.getUser();
    }


    //如果上面supportsParameter,返回 T,就执行下面的resolveArgument方法
    //到底怎么解析，是由程序员根据业务来编写
    //@Override
    //public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    //
    //    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
    //    HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
    //
    //    String ticket = CookieUtil.getCookieValue(request, "userTicket");
    //    if (!StringUtils.hasText(ticket)) {
    //        return null;
    //    }
    //    //不急，一会老师会给小伙伴debug
    //    ////从Redis来获取用户
    //    User user = userService.getUserByCookieByRedis(ticket, request, response);
    //
    //    return user;
    //    //return UserContext.getUser();
    //}
}
