package com.rainbowsea.seckill.rabbitmq;


import com.rainbowsea.seckill.config.RabbitMQSecKillConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 消息的生产者/发送者 发送【秒杀消息】
 */
@Slf4j
@Service
public class MQSenderMessage {

    // 装配 RabbitTemplate
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送者，将信息发送给交换机
     *
     * @param message
     */
    public void sendSeckillMessage(String message) {
        log.info("发送消息: " + message);
        rabbitTemplate.convertAndSend(RabbitMQSecKillConfig.EXCHANGE,
                "seckill.message",  // 对应队列的 routingKey
                message);
    }
}
