package com.rainbowsea.seckill.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author RainbowSea
 * @version 1.0
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    //装配
    @Resource
    private UserArgumentResolver userArgumentResolver;


    // 装配自己编写的自定义拦截器
    @Resource
    private AccessLimitInterceptor accessLimitInterceptor;

    // 将自定义的拦截器装配进去才有效果
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLimitInterceptor);
    }

    //静态资源加载
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    //这里加入我们自定义的解析器到 HandlerMethodArgumentResolver列表中
    //这样自定义的解析器工作
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }


}
