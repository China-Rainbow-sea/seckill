package com.rainbowsea.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.rainbowsea.seckill.mapper.OrderMapper;
import com.rainbowsea.seckill.pojo.Order;
import com.rainbowsea.seckill.pojo.SeckillGoods;
import com.rainbowsea.seckill.pojo.SeckillOrder;
import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.service.OrderService;
import com.rainbowsea.seckill.service.SeckillGoodsService;
import com.rainbowsea.seckill.service.SeckillOrderService;
import com.rainbowsea.seckill.vo.GoodsVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author huo
 * @description 针对表【t_order】的数据库操作Service实现
 * @createDate 2025-04-26 20:48:19
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements OrderService {

    @Resource
    private SeckillGoodsService seckillGoodsService;

    @Resource
    private OrderMapper orderMapper;


    @Resource
    private SeckillOrderService seckillOrderService;


    /**
     * 秒杀商品，减少库存
     *
     * @param user
     * @param goodsVo
     * @return Order
     */
    @Override
    public Order seckill(User user, GoodsVo goodsVo) {

        // 查询后端的库存量进行减一
        SeckillGoods seckillGoods = seckillGoodsService
                .getOne(new QueryWrapper<SeckillGoods>()
                        .eq("goods_id", goodsVo.getId()));

        // 完成一个基本的秒杀操作【这快不具原子性】，后面在高并发的情况下，还会优化
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        seckillGoodsService.updateById(seckillGoods);

        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L); // 这里随便设置了一个初始值
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());

        // 保存 order 账单信息
        orderMapper.insert(order);

        // 生成秒杀商品订单~
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setGoodsId(goodsVo.getId());
        // 这里秒杀商品订单对应的 order_id 是从上面添加 order 后获取到的
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(user.getId());

        // 保存 seckillOrder
        seckillOrderService.save(seckillOrder);

        return order;
    }
}




