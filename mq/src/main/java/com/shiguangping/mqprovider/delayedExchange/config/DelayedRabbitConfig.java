package com.shiguangping.mqprovider.delayedExchange.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@SpringBootConfiguration
public class DelayedRabbitConfig {

    public static final String DELAYED_EXCHANGE = "topic.exchange.delayed";
    public static final String DELAYED_QUEUE = "topic.queue.test.delayed";
    public static final String DELAYED_QUEUE_ROUTINGKEY = "topic.routing-key.delayed";

    /**
     *RabbitMQ 安装延迟队列插件(六)
     */

// 主要交换机
    @Bean
    public CustomExchange DelayedExchangeNormal() {
        Map<String, Object> map = new HashMap<>();
        map.put("x-delayed-type", "direct");
        /**
         * 1.交换机
         * 2.类型
         * 3.是否持久化
         * 4.是否自动删除
         * 5其他参数
         */
        return new CustomExchange(DELAYED_EXCHANGE, "x-delayed-message", true, false, map);
    }

    @Bean
    public Queue DelayedQueueNormal() {
        return new Queue(DELAYED_QUEUE);
    }

    @Bean
    public Binding DelayedQueueBindingExchangeNormal() {
        return BindingBuilder.bind(DelayedQueueNormal()).to(DelayedExchangeNormal()).with(DELAYED_QUEUE_ROUTINGKEY).noargs();
    }

}
