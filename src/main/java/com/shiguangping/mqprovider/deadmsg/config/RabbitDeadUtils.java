package com.shiguangping.mqprovider.deadmsg.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
@Slf4j
public class RabbitDeadUtils {

    //由于rabbitTemplate的scope属性设置为ConfigurableBeanFactory.SCOPE_PROTOTYPE，所以不能自动注入
    private RabbitTemplate rabbitTemplate;


    /**
     * 构造方法注入rabbitTemplate
     */
    @Autowired
    public RabbitDeadUtils(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
//        rabbitTemplate.setConfirmCallback(this); //rabbitTemplate如果为单例的话，那回调就是最后设置的内容
//        rabbitTemplate.setReturnsCallback(this);// 指定 ReturnCallback
//        rabbitTemplate.setConfirmCallback(this::confirm);
//        rabbitTemplate.setReturnsCallback(this::returnedMessage);
    }

    public void sendMsg(String model) {
        CorrelationData correlationData1 = new CorrelationData("1");
        //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
        rabbitTemplate.convertAndSend(DeadRabbitConfig.EXCHANGE_A, DeadRabbitConfig.ROUTINGKEY_A, model, correlationData1);

        //  写错误的routingkey 触发setReturnCallback
        CorrelationData correlationData2 = new CorrelationData("2");
        //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
        rabbitTemplate.convertAndSend(DeadRabbitConfig.EXCHANGE_A, DeadRabbitConfig.ROUTINGKEY_A + "2", model, correlationData2);
    }

    public void sendPriorityQueue(String msg, Integer priority) {
        String uuid = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(uuid);
        log.info("msg:{},priority:{},uuid:{}", msg, priority, uuid);
        //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
        rabbitTemplate.convertAndSend(DeadRabbitConfig.EXCHANGE_A, DeadRabbitConfig.ROUTINGKEY_A, msg, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            // 设置这条消息的过期时间
            messageProperties.setPriority(priority);
            messageProperties.setMessageId(uuid);
            return message;
        }, correlationData);
    }

    public void sendMsgReturn(String s) {
        MessageConverter converter = rabbitTemplate.getMessageConverter();
        MessageProperties props = new MessageProperties();
//        props.setHeader("X_ORDER_SOURCE", "WEB");
        Message message = converter.toMessage(s, props);
        rabbitTemplate.send(DeadRabbitConfig.EXCHANGE_A, DeadRabbitConfig.ROUTINGKEY_A, message);
    }

    //    设置单条信息的过期时间
    public void sendMsgTimeToLive(String model) {
        UUID uuid = UUID.randomUUID();
        CorrelationData correlationData2 = new CorrelationData(uuid.toString());
        //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
        rabbitTemplate.convertAndSend(DeadRabbitConfig.EXCHANGE_A, DeadRabbitConfig.ROUTINGKEY_A, model, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            // 设置这条消息的过期时间
            messageProperties.setExpiration("5000");
            return message;
        }, correlationData2);
    }


//    /**
//     *  publisher-confirm-type: correlated
//     * 确认放入交换机回调
//     */
//    @Override
//    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//        log.info("确认放入交换机回调id:" + correlationData);
//        if (ack) {
//            log.info("确认放入交换机回调入队成功");
//        } else {
//            log.info("确认放入交换机回调入队失败:" + cause);
//        }
//    }
//
//
//
//    /**
//     * 确认放入队列回调
//     * * publisher-returns: true
//     */
//    @Override
//    public void returnedMessage(ReturnedMessage returnedMessage) {
//        log.error("消息{},被交换机{}退回,退回的原因:{},路由key是:{}",new String(returnedMessage.getMessage().getBody()),
//                returnedMessage.getExchange(),returnedMessage.getReplyText(),returnedMessage.getRoutingKey());
//    }

}
