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
import com.rainbowsea.seckill.utill.MD5Util;
import com.rainbowsea.seckill.utill.UUIDUtil;
import com.rainbowsea.seckill.vo.GoodsVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

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

        if (!update) {  // 如果更新失败，说明已经没有库存了
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
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" +
                        goodsVo.getId(),
                seckillOrder);

        return order;
    }


    /**
     * 生成秒杀路径/值（唯一）
     *
     * @param user    用户对象
     * @param goodsId 对应秒杀商品ID
     * @return String 返回唯一路径
     * 同时将生成的路径，存入到 Redis 当中，同时设计以一个失效时间 60s,该时间内没访问，就失效
     */
    @Override
    public String createPath(User user, Long goodsId) {
        // 生成秒杀路径/值唯一
        String path = MD5Util.md5(UUIDUtil.uuid());
        // 将随机生成的路径保存到 Redis，同时设置一个超时时间 60s，
        // 60s 不访问，这个秒杀路径就失效
        // Redis 当中 key 的设计: seckillPath:userId:goodsId
        redisTemplate.opsForValue().set("seckillPath:"
                + user.getId() + ":" + goodsId, path, 60, TimeUnit.SECONDS);
        return path;
    }


    /**
     * 对秒杀路径进行校验
     *
     * @param user    用户对象
     * @param goodsId 对应秒杀商品ID
     * @param path    校验的秒杀路径
     * @return boolean 秒杀路径正确，返回 true ，否则返回 false
     */
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId < 0 || !StringUtils.hasText(path)) {
            return false;
        }

        // 从 Redis 当中获取该用户秒杀该商品的路径
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:"
                + user.getId() + ":" + goodsId);
        // 判断这两个路径是否相同，相同说明正确，不相同说明错误
        return path.equals(redisPath);
    }


    /**
     * 验证用户输入的验证码是否正确
     *
     * @param user    用户信息对象
     * @param goodsId 秒杀商品ID
     * @param captcha 需要验证的验证码
     * @return boolean 通过返回 true，验证失败返回 false
     */
    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {

        if (user == null || goodsId < 0 || !StringUtils.hasText(captcha)) {
            return false;
        }

        // 从 Redis 取出验证码,注意:怎么存的key，就怎么取
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);


        return captcha.equals(redisCaptcha);
    }
}




