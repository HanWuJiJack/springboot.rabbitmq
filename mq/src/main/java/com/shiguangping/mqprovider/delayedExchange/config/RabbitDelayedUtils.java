package com.shiguangping.mqprovider.delayedExchange.config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class RabbitDelayedUtils {

    //由于rabbitTemplate的scope属性设置为ConfigurableBeanFactory.SCOPE_PROTOTYPE，所以不能自动注入
    private RabbitTemplate rabbitTemplate;

    /**
     * 构造方法注入rabbitTemplate
     */
    @Autowired
    public RabbitDelayedUtils(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    //    设置单条信息的过期时间
    public void sendMsgTimeToLive(String model) {
        UUID uuid = UUID.randomUUID();
        CorrelationData correlationData = new CorrelationData(uuid.toString());
        SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format0.format(new Date().getTime());//这个就是把时间戳经过处理得到期望格
        log.info("开始时间{}，",time);
        //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
        rabbitTemplate.convertAndSend(DelayedRabbitConfig.DELAYED_EXCHANGE, DelayedRabbitConfig.DELAYED_QUEUE_ROUTINGKEY, model, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            //发送消息 并设置delayedTime
            messageProperties.setDelay(5000);
            return message;
        },correlationData);
    }
}
