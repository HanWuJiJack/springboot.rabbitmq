package com.shiguangping.mqprovider.alternateexchange.config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RabbitAlternateUtils {

    //由于rabbitTemplate的scope属性设置为ConfigurableBeanFactory.SCOPE_PROTOTYPE，所以不能自动注入
    private RabbitTemplate rabbitTemplate;

    /**
     * 构造方法注入rabbitTemplate
     */
    @Autowired
    public RabbitAlternateUtils(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMsg(String s) {
        UUID id = UUID.randomUUID();
        CorrelationData correlationData = new CorrelationData(id.toString());
        //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
        rabbitTemplate.convertAndSend(AlternateRabbitConfig.EXCHANGE_A, AlternateRabbitConfig.ROUTINGKEY_A, s, correlationData);

        rabbitTemplate.convertAndSend(AlternateRabbitConfig.EXCHANGE_A, AlternateRabbitConfig.ROUTINGKEY_A + "1" , s, correlationData);
    }
}
