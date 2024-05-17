package com.shiguangping.mqprovider.priorityExchange.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RabbitPriorityUtils {

    //由于rabbitTemplate的scope属性设置为ConfigurableBeanFactory.SCOPE_PROTOTYPE，所以不能自动注入
    private RabbitTemplate rabbitTemplate;


    /**
     * 构造方法注入rabbitTemplate
     */
    @Autowired
    public RabbitPriorityUtils(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendPriorityQueue(String msg, Integer priority) {
        String uuid = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(uuid);
        log.info("msg:{},priority:{},uuid:{}", msg, priority, uuid);
        //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
        rabbitTemplate.convertAndSend(PriorityRabbitConfig.EXCHANGE_A, PriorityRabbitConfig.ROUTINGKEY_A, msg, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            // 设置这条消息的优先级
            messageProperties.setPriority(priority);
            messageProperties.setMessageId(uuid);
            return message;
        }, correlationData);
    }


}
