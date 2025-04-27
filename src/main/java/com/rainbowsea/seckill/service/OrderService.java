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
}
