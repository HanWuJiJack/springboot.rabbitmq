package com.shiguangping.mqprovider.delayedExchange.consumer;

import com.rabbitmq.client.Channel;
import com.shiguangping.mqprovider.delayedExchange.config.DelayedRabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
@RabbitListener(queues= DelayedRabbitConfig.DELAYED_QUEUE, ackMode="MANUAL")
public class DelayedReceiver {
    @RabbitHandler
    public void processHandler(String msg, Channel channel, Message message) throws IOException {
        channel.basicQos(1);
        try {
            SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format0.format(new Date().getTime());//这个就是把时间戳经过处理得到期望格
            log.info("DELAYED_QUEUE 收到消息：{}", msg);
            log.info("结束时间{}，",time);
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

