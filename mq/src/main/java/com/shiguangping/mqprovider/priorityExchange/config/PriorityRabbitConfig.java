package com.shiguangping.mqprovider.priorityExchange.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootConfiguration
public class PriorityRabbitConfig {

    public static final String EXCHANGE_A = "Priority-my-mq-exchange_A";

    public static final String QUEUE_A = "Priority-QUEUE-A";

    public static final String ROUTINGKEY_A = "Priority-spring-boot-routingKey_A";

    @Bean
    public DirectExchange PriorityExchangeA() {
        return new DirectExchange(EXCHANGE_A);
    }


    @Bean
    public Queue PriorityqueueA() {

        Map<String, Object> args = new HashMap<>(2);
        //优先级队列 0-255 建议取0-10
        args.put("x-max-priority",10);
        Queue queue = new Queue(QUEUE_A, false, false, false, args);
        return queue;
    }


    @Bean
    public Binding PriorityqueueBindingExchange(){
        return BindingBuilder.bind(PriorityqueueA()).to(PriorityExchangeA()).with(PriorityRabbitConfig.ROUTINGKEY_A);
    }


}
