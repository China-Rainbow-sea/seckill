package com.rainbowsea.seckill.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SeckillMessage 秒杀消息对象，用于 RabbitMQ 消息队列进行发送传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage {

    private User user;

    private Long goodsId;
}


