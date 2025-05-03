package com.rainbowsea.seckill.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Queue;

/**
 * 配置类，RabbitMQ 创建消息队列和交换机，以及消息队列和交换机的之间的关系
 */
@Configuration
public class RabbitMQSecKillConfig {

    // 定义消息队列和交换机名
    public static final String QUEUE = "seckillQueue";

    public static final String EXCHANGE = "seckillExchange";


    /**
     * 创建队列
     *
     * @return Queue 队列
     */
    @Bean // 没有指明 value ，默认就是方法名
    public Queue queue_seckill() {
        return new Queue(QUEUE);
    }


    /**
     * @return TopicExchange 主题交换机
     */
    @Bean
    public TopicExchange topicExchange_seckill() {
        return new TopicExchange(EXCHANGE);
    }


    /**
     * 将队列绑定到对应的交换机当中，并指定路由,"主题"（哪些信息发送给 seckill.# 哪个队列）
     *
     * @return
     */
    @Bean
    public Binding binding_seckill() {
        return BindingBuilder.bind(queue_seckill()).to(topicExchange_seckill())
                .with("seckill.#");
    }


}
