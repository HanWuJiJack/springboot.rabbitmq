package com.shiguangping.mqprovider.deadmsg.consumer;
import com.shiguangping.mqprovider.deadmsg.config.DeadRabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RabbitListener(queues= DeadRabbitConfig.QUEUE_Dead)
public class ReceiverDead {
    @RabbitHandler
    public void process(String s) {
        log.info("dead-消费消息：{}", s);
    }
}

