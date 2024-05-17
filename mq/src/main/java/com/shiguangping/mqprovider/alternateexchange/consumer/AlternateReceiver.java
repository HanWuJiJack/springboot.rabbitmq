package com.shiguangping.mqprovider.alternateexchange.consumer;

import com.shiguangping.mqprovider.alternateexchange.config.AlternateRabbitConfig;
import com.shiguangping.mqprovider.deadmsg.config.DeadRabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = AlternateRabbitConfig.QUEUE_alternate)
public class AlternateReceiver {
    @RabbitHandler
    public void process(String model) {
        System.out.println("Alternate备用消息： " + model
        );
    }
}

