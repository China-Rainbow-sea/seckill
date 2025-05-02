package com.rainbowsea.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 秒杀商品，减少库存，V1.0 没有进行复购处理
     *
     * @param user
     * @param goodsVo
     * @return Order
     */
    //@Override
    //public Order seckill(User user, GoodsVo goodsVo) {
    //
    //    // 查询后端的库存量进行减一
    //    SeckillGoods seckillGoods = seckillGoodsService
    //            .getOne(new QueryWrapper<SeckillGoods>()
    //                    .eq("goods_id", goodsVo.getId()));
    //
    //    // 完成一个基本的秒杀操作【这快不具原子性】，后面在高并发的情况下，还会优化
    //    seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
    //    seckillGoodsService.updateById(seckillGoods);
    //
    //    Order order = new Order();
    //    order.setUserId(user.getId());
    //    order.setGoodsId(goodsVo.getId());
    //    order.setDeliveryAddrId(0L); // 这里随便设置了一个初始值
    //    order.setGoodsName(goodsVo.getGoodsName());
    //    order.setGoodsCount(1);
    //    order.setGoodsPrice(seckillGoods.getSeckillPrice());
    //
    //    // 保存 order 账单信息
    //    orderMapper.insert(order);
    //
    //    // 生成秒杀商品订单~
    //    SeckillOrder seckillOrder = new SeckillOrder();
    //    seckillOrder.setGoodsId(goodsVo.getId());
    //    // 这里秒杀商品订单对应的 order_id 是从上面添加 order 后获取到的
    //    seckillOrder.setOrderId(order.getId());
    //    seckillOrder.setUserId(user.getId());
    //
    //    // 保存 seckillOrder
    //    seckillOrderService.save(seckillOrder);
    //
    //    return order;
    //}


    /**
     * 秒杀商品，减少库存，V2.0 利用 MySQL默认的事务隔离级别【REPEATABLE-READ】 ,
     * 添加上 @Transactional，注解进行一个MySQL 默认的事务隔离级别
     *
     * @param user
     * @param goodsVo
     * @return Order
     */
    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goodsVo) {

        // 查询后端的库存量进行减一
        SeckillGoods seckillGoods = seckillGoodsService
                .getOne(new QueryWrapper<SeckillGoods>()
                        .eq("goods_id", goodsVo.getId()));

        // 完成一个基本的秒杀操作【这快不具原子性】，后面在高并发的情况下，还会优化
        //seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        //seckillGoodsService.updateById(seckillGoods);

        //分析
        // 1. MySQL 在默认的事务隔离级别 【REPEATABLE-READ】 下
        // 2. 执行 update 语句时，会在事务中锁定要更新的行
        // 3. 这样可以防止其它会话在同一行执行 update,delete

        // 说明: 只要在更新成功时，返回 true，否则返回 false
        // column 必须是，数据库表当中的字段，不可以随便写
        boolean update = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = stock_count - 1")
                .eq("goods_id", goodsVo.getId())
                .gt("stock_count", 0));  // gt  表示大于

        if(!update) {  // 如果更新失败，说明已经没有库存了
            return null;
        }

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
        // 将生成的秒杀订单,存入到 Redis,这样在查询某个用户是否已经秒杀了这个商品时
        // 直接到 Redis 中查询，起到优化效果
        // key表示:order:userId:goodsId  Value表示订单 seckillOrder
        redisTemplate.opsForValue().set("order:"+user.getId()+":" +
                goodsVo.getId(),
                seckillOrder);

        return order;
    }
}




