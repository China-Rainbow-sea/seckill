package com.rainbowsea.seckill.config;


import com.rainbowsea.seckill.pojo.User;

/**
 * 用来存储拦截器获取的 user对象.
 */
public class UserContext {
    // 每个线程都有自己的 ThreadLocal ，把共享数据存放到这里，保证线程安全
    private static ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static User getUser() {
        return userHolder.get();
    }

    public static void setUser(User user) {
        userHolder.set(user);
    }

}
