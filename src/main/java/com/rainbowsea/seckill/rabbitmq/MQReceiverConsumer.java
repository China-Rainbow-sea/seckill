package com.rainbowsea.seckill.rabbitmq;


import cn.hutool.json.JSONUtil;
import com.rainbowsea.seckill.config.RabbitMQSecKillConfig;
import com.rainbowsea.seckill.pojo.SeckillMessage;
import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.service.GoodsService;
import com.rainbowsea.seckill.service.OrderService;
import com.rainbowsea.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 消息的接收者/消费者，接收生产者，发送过来的信息
 * ,接收到信息后，调用秒杀商品的方法，orderService.seckill(user, goodsVo);
 */
@Service
@Slf4j
public class MQReceiverConsumer {

    @Resource
    private GoodsService goodsService;


    @Resource
    private OrderService orderService;


    /**
     * 接受这个 queues = RabbitMQSecKillConfig.QUEUE 队列的当中的信息
     *
     * @param message 生产者发送的信息，其实就是 seckillMessage 对象信息，被我们转换为了 JSON
     *                格式的 String
     */
    @RabbitListener(queues = RabbitMQSecKillConfig.QUEUE)
    public void queue(String message) {
        log.info("接收到的消息是: " + message);
        /*
        这里我么们从队列中取出的是 String 类型
        但是，我们需要的是 SeckillMessage，因此需要一个工具类 JSONUtil
        ,该工具需要引入 hutool 工具类的 jar 包
         */
        SeckillMessage seckillMessage = JSONUtil.toBean(message, SeckillMessage.class);

        // 秒杀用户对象
        User user = seckillMessage.getUser();

        // 秒杀用户的商品ID
        Long goodsId = seckillMessage.getGoodsId();

        // 通过商品ID，得到对应的 GoodsVo 秒杀商品信息对象
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        // 下单操作
        orderService.seckill(user, goodsVo);

    }

}
