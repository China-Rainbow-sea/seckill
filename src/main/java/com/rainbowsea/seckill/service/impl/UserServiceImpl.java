package com.rainbowsea.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rainbowsea.seckill.exception.GlobalException;
import com.rainbowsea.seckill.mapper.UserMapper;
import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.service.UserService;
import com.rainbowsea.seckill.utill.CookieUtil;
import com.rainbowsea.seckill.utill.MD5Util;
import com.rainbowsea.seckill.utill.UUIDUtil;
import com.rainbowsea.seckill.utill.ValidatorUtil;
import com.rainbowsea.seckill.vo.LoginVo;
import com.rainbowsea.seckill.vo.RespBean;
import com.rainbowsea.seckill.vo.RespBeanEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author huo
 * @description 针对表【seckill_user】的数据库操作Service实现
 * @createDate 2025-04-24 15:38:01
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;


    /**
     * 登录校验
     *
     * @param loginVo  登录时发送的信息
     * @param request  request
     * @param response response
     * @return RespBean
     */
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {

        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        // 判断手机号/id，和密码是否为空
        //if (!StringUtils.hasText(mobile) || !StringUtils.hasText(password)) {
        //    return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        //}

        // 判断手机号是否合格
        //if (!ValidatorUtil.isMobile(mobile)) {
        //    return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        //}

        // 查询DB,判断用户是否存在
        User user = userMapper.selectById(mobile);
        if (null == user) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }


        // 如果用户存在，则对比密码!
        // 注意:我们从 LoginVo 取出的密码是中间密码(即客户端经过一次加密加盐处理的密码)
        if (!MD5Util.midPassToDBPass(password, user.getSlat()).equals(user.getPassword())) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }

        // 登录成功

        // 给每个用户生成 ticket 唯一
        String ticket = UUIDUtil.uuid();

        // 为实现分布式 Session ，把登录用户信息存放到 Redis 当中
        System.out.println("使用 redisTemplate->" + redisTemplate.hashCode());
        redisTemplate.opsForValue().set("user:" + ticket, user);


        // 将登录成功的用户保存到 session
        //request.getSession().setAttribute(ticket, user);

        // 将 ticket 保存到 cookie,cookieName 不可以随便写，必须时 "userTicket"
        CookieUtil.setCookie(request, response, "userTicket", ticket);

        //返回 ticket,否则生成多用户脚本时, userTicket 是 null
        return RespBean.success(ticket);
    }

    /**
     * 根据 Cookie 当中的 userTicket 获取判断，存储到 Redis 当中的用户信息
     *
     * @param userTicket Cookie 当中的 userTicket
     * @param request
     * @param response
     * @return 存储到 Redis 当中的 User 对象信息
     */
    @Override
    public User getUserByCookieByRedis(String userTicket, HttpServletRequest request, HttpServletResponse response) {

        if (!StringUtils.hasText(userTicket)) {
            return null;
        }

        // 根据 Cookie 当中的 userTicket 获取判断，存储到 Redis 当中的用户信息
        // 注意：这里我们在 Redis 存储的 Key是:"user:+userTicket"
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);

        // 如果用户不为 null，就重新设置 cookie,刷新，防止cookie超时了，
        if (user != null) {
            // cookieName 不可以随便写，必须是 "userTicket"
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }

        return user;
    }


    /**
     * 更改用户密码，同时立马更新缓存到 Redis 当中的信息
     * @param userTicket
     * @param password
     * @param request
     * @param response
     * @return
     */
    @Override
    public RespBean updatePassword(String userTicket,
                                   String password,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {

        // 更新用户密码,同时删除用户在 Redis的缓存对象
        User user = this.getUserByCookieByRedis(userTicket,request,response);

        if(user == null) {
            // 抛出异常
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }

        // 设置新密码
        user.setPassword(MD5Util.inputPassToDBPass(password,user.getSlat()));

        int i = userMapper.updateById(user);
        if(i == 1) {  // 更新成功
            redisTemplate.delete("user:"+userTicket);
            return RespBean.success();
        }


        return RespBean.error(RespBeanEnum.PASSWROD_UPDATE_FAIL);
    }


}




