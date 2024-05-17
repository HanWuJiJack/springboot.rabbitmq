package com.shiguangping.mqprovider.entityExchange.config;

import cn.hutool.json.JSONUtil;
import com.shiguangping.mqprovider.entity.RabbitMQRequestEntity;
import com.shiguangping.mqprovider.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RabbitEntityUtils {

    //由于rabbitTemplate的scope属性设置为ConfigurableBeanFactory.SCOPE_PROTOTYPE，所以不能自动注入
    private RabbitTemplate rabbitTemplate;


    /**
     * 构造方法注入rabbitTemplate
     */
    @Autowired
    public RabbitEntityUtils(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send() {
        String uuid = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(uuid);
        User user = new User();
        user.setId("123");
        user.setMessage("name");
        RabbitMQRequestEntity build = RabbitMQRequestEntity.builder()
                .businessId(UUID.randomUUID().toString())
                .data(user)
                .description("测试一下配置功能")
                .sendApplicationName("rabbitmq")
                .sendTopic("rabbitmq_发送测试demo")
                .build();


        correlationData.setReturnedMessage(new Message(JSONUtil.toJsonStr(build).getBytes(),  new MessageProperties()));
        rabbitTemplate.convertAndSend(EntityRabbitConfig.EXCHANGE_A, EntityRabbitConfig.ROUTINGKEY_A + "1", build, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
//            messageProperties.setMessageId(uuid);
            // 请求头 为json
            messageProperties.setHeader("content_type", MessageProperties.CONTENT_TYPE_JSON);
            return message;
        }, correlationData);
    }


}
