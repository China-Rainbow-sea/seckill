package com.rainbowsea.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rainbowsea.seckill.pojo.Order;
import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.vo.GoodsVo;

/**
* @author huo
* @description 针对表【t_order】的数据库操作Service
* @createDate 2025-04-26 20:48:19
*/
public interface OrderService extends IService<Order> {


    /**
     * 秒杀
     * @param user
     * @param goodsVo
     * @return Order 账单信息
     */
    Order seckill(User user, GoodsVo goodsVo);


    /**
     * 生成秒杀路径/值（唯一）
     * @param user 用户对象
     * @param goodsId 对应秒杀商品ID
     * @return String 返回唯一路径
     */
    public String createPath(User user,Long goodsId);

    /**
     * 对秒杀路径进行校验
     * @param user 用户对象
     * @param goodsId 对应秒杀商品ID
     * @param path 校验的秒杀路径
     * @return boolean 秒杀路径正确，返回 true ，否则返回 false
     */
    public boolean checkPath(User user,Long goodsId,String path);

    /**
     * 验证用户输入的验证码是否正确
     * @param user 用户信息对象
     * @param goodsId 秒杀商品ID
     * @param captcha  需要验证的验证码
     * @return boolean 通过返回 true，验证失败返回 false
     */
    boolean checkCaptcha(User user,Long goodsId,String captcha);
}
