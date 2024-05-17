package com.shiguangping.mqprovider.deadmsg.consumer;

import com.rabbitmq.client.Channel;
import com.shiguangping.mqprovider.deadmsg.config.DeadRabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RabbitListener(queues= DeadRabbitConfig.QUEUE_A, ackMode="MANUAL")
public class DeadReceiverA {
    @RabbitHandler
    public void processHandler(String msg, Channel channel, Message message) throws IOException {
        try {
            Thread.sleep(1000);
            log.info("dead-A-消费消息：{};uuid:{}", msg, message.getMessageProperties().getMessageId());
            //TODO 具体业务

            //告诉服务器收到这条消息 无需再发了 否则消息服务器以为这条消息没处理掉 后续还会在发
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }  catch (Exception e) {
            log.info("回调的相关数据:{} ack:{} cause:{} ");
            if (message.getMessageProperties().getRedelivered()) {
                log.error("消息已重复处理失败,拒绝再次接收...");

//                deliveryTag：消息的传递标识。
//                requeue： 设置为false 表示不再重新入队，如果配置了死信队列则进入死信队列。
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                log.error("消息即将再次返回队列处理...");
                //                拒绝一个或多个消息：
//                void basicNack(long deliveryTag, boolean multiple, boolean requeue) throws IOException;
//                deliveryTag：消息的传递标识。
//                multiple： 如果为true，则拒绝所有consumer获得的小于deliveryTag的消息。
//                requeue： 设置为true 会把消费失败的消息从新添加到队列的尾端，设置为false不会重新回到队列。
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                throw new RuntimeException("自定义");
            }
        }
    }

}

