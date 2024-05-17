package com.shiguangping.mqprovider.deadmsg.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootConfiguration
public class DeadRabbitConfig {

    public static final String EXCHANGE_A = "dead-my-mq-exchange_A";
    public static final String EXCHANGE_Dead = "dead-my-mq-exchange_dead";

    public static final String QUEUE_A = "dead-QUEUE-A";
    public static final String QUEUE_Dead = "dead-QUEUE-dead";

    public static final String ROUTINGKEY_A = "dead-spring-boot-routingKey_A";
    public static final String ROUTINGKEY_Dead = "dead-spring-boot-routingKey_dead";

    /**
     * 针对消费者配置
     * 1. 设置交换机类型
     * 2. 将队列绑定到交换机
     FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念
     HeadersExchange ：通过添加属性key-value匹配
     DirectExchange:按照routingkey分发到指定队列
     TopicExchange:多关键字匹配
     */

//    如何进入死信队列
//    消息被拒绝（reject ，nack），并且 requeue = false（不再重新投递）
//    消息 TTL 过期
//    队列超过最长长度
    @Bean
    public DirectExchange ExchangeA() {
        return new DirectExchange(EXCHANGE_A);
    }

    @Bean
    public DirectExchange ExchangeDead() {
        return new DirectExchange(EXCHANGE_Dead);
    }
    /**
     * 获取队列A
     * @return
     */
    @Bean
    public Queue queueA() {
////        return new Queue(QUEUE_A, true); //队列持久
//        Map<String, Object> arguments = new HashMap<>();
////        arguments.put("x-message-ttl", 5000);  // 设置队列消息过期时间
//        //x-dead-letter-exchange 这里声明当前队列绑定的死信交换机
//        arguments.put("x-dead-letter-exchange", EXCHANGE_Dead);
//        // x-dead-letter-routing-key 这里声明当前队列的死信路由key
//        arguments.put("x-dead-letter-routing-key",ROUTINGKEY_Dead);
//        return QueueBuilder.durable(QUEUE_A).withArguments(arguments).build();
        Map<String, Object> args = new HashMap<>(2);
        //交换机标识符
        args.put("x-dead-letter-exchange", EXCHANGE_Dead);
        //绑定键标识符
        args.put("x-dead-letter-routing-key", ROUTINGKEY_Dead);
        Queue queue = new Queue(QUEUE_A, false, false, false, args);
//        Queue queue = new Queue(QUEUE_A, false, false, false);
        return queue;
    }

// 声明死信队列A
    @Bean("deadLetterQueue")
    public Queue deadLetterQueue(){
        // 队列持久化代码
        // 参数1 name ：队列名
        // 参数2 durable ：是否持久化
        // 参数3 exclusive ：仅创建者可以使用的私有队列，断开后自动删除
        // 参数4 autoDelete : 当所有消费客户端连接断开后，是否自动删除队列
        return new Queue(QUEUE_Dead,false,false,true);
    }

    /**
     * 把交换机，队列，通过路由关键字进行绑定
     * @return
     */
//    @Bean
//    public Binding binding() {
//        return BindingBuilder.bind(queueA()).to(ExchangeA()).with(DeadRabbitConfig.ROUTINGKEY_A);
//    }

    @Bean
    public Binding queueBindingExchange(@Qualifier("queueA")Queue queueA,
                                        @Qualifier("ExchangeA")DirectExchange ExchangeA ){
        return BindingBuilder.bind(queueA).to(ExchangeA).with(DeadRabbitConfig.ROUTINGKEY_A);
    }


    // 声明死信队列A绑定关系
//    @Bean
//    public Binding deadLetterBindingA(){
//        return BindingBuilder.bind(deadLetterQueue()).to(ExchangeDead()).with(ROUTINGKEY_Dead);
//    }

    @Bean
    public Binding deadLetterBindingA(@Qualifier("deadLetterQueue")Queue deadLetterQueue,
                                        @Qualifier("ExchangeDead")DirectExchange ExchangeDead ){
        return BindingBuilder.bind(deadLetterQueue).to(ExchangeDead).with(ROUTINGKEY_Dead);
    }

}
