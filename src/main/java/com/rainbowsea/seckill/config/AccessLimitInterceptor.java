package com.rainbowsea.seckill.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.service.UserService;
import com.rainbowsea.seckill.utill.CookieUtil;
import com.rainbowsea.seckill.vo.RespBean;
import com.rainbowsea.seckill.vo.RespBeanEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;


/**
 * 自定义的拦截器
 */

@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    // 装配需要的组件/对象
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;


    /**
     * 拦截器最前面执行
     *
     * @param request
     * @param response
     * @param handler
     * @return boolean 是否放行
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            // 这里我们就先获取到登录的 user 对象
            User user = getUser(request, response);

            // 存入到 ThreadLocal
            UserContext.setUser(user);

            // 把handler 转成 HandlerMethod
            HandlerMethod hm = (HandlerMethod) handler;

            // 获取到目标方法的注解
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);

            if (accessLimit == null) { // 如果目标方法没有 @AccessLimit 注解,说明接口并没有处理限流防刷
                return true;
            }

            // 获取注解的值
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            if (needLogin) { // 说明用户必须登录才能访问目标方法/接口
                if (user == null) { // 说明用户没有登录
                    // 返回一个用户信息错误的提示，
                    this.render(response, RespBeanEnum.SESSION_ERROR);
                    return false; // 返回
                }

            }

            // 增加业务逻辑: 加入 Redis 计数器，完成对用户的限流防刷
            // 比如: 5 秒内访问次数超过 5 次，我们就认为是刷接口
            // 这里老师先把代码写在方法中，后面我们使用注解提高使用的通用性
            // uri 就是 localhost:8080/seckill/path 当中的 /seckill/path
            String uri = request.getRequestURI();
            ValueOperations valueOperations = redisTemplate.opsForValue();
            // 存储到 Redis 当中，key uri + ":" + user.getId()
            String key = uri + ":" + user.getId();
            Integer count = (Integer) valueOperations.get(key);

            if (count == null) {  // 说明还没有 key,就初始化: 值为 1，过期时间为 5 秒
                valueOperations.set(key, 1, second, TimeUnit.SECONDS);
            } else if (count < maxCount) { // 说明正常访问
                valueOperations.increment(key); // -1
            } else { // > 5 说明用户在刷接口
                // 返回一个用户信息错误的提示，
                this.render(response, RespBeanEnum.ACCESS_LIMIT_REACHED);
                return false; // 返回
            }

        }

        // 最后，都没啥限制，就放行
        return true;
    }


    /**
     * 构建返回对象-以流的形式返回，上述 preHandle 的返回信息给前端
     *
     * @param response
     * @param respBeanEnum
     */
    private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
        // 构建 RespBean
        RespBean error = RespBean.error(respBeanEnum);
        out.write(new ObjectMapper().writeValueAsString(error));
        out.flush();
        out.close();
    }


    /**
     * 获取到 Cookie 当中存储的 User 对象，注意：key 是 "userTicket" 不可以随便写
     *
     * @param request
     * @param response
     * @return User
     */
    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if (!StringUtils.hasText(ticket)) {
            return null;  // 说明该用户没有登录，直接返回 null
        }

        return userService.getUserByCookieByRedis(ticket, request, response);
    }
}
