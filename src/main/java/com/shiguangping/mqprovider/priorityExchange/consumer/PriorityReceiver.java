package com.shiguangping.mqprovider.priorityExchange.consumer;

import com.rabbitmq.client.Channel;
import com.shiguangping.mqprovider.priorityExchange.config.PriorityRabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RabbitListener(queues= PriorityRabbitConfig.QUEUE_A, ackMode="MANUAL")
public class PriorityReceiver {
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
                // 拒绝消息
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                log.error("消息即将再次返回队列处理...");
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                throw new RuntimeException("自定义");
            }
        }
    }

}

