package com.shiguangping.mqprovider.config;

import cn.hutool.json.JSONUtil;
import com.shiguangping.mqprovider.entity.RabbitMQRequestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class RabbitConfigCallback implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitConfig rabbitConfig;


    public static final MessagePostProcessor messagePostProcessor =
            new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setHeader("content_type", MessageProperties.CONTENT_TYPE_JSON);
                    return message;
                }
            };


    /**
     * ==================设置请求body数据结构为：json=====================
     */
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitConfig.producerJackson2MessageConverter());
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    /**
     * ==================配置admin管理MQ相关信息=====================
     */
    @Bean
    public RabbitAdmin rabbitAdmin(final ConnectionFactory createConnectionFactory) {
        return new RabbitAdmin(createConnectionFactory);
    }

    /**
     * confirm机制只保证消息到达exchange，不保证消息可以路由到正确的queue,如果exchange错误，就会触发confirm机制
     *
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        // 消息发送时间

        SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = format0.format(new Date().getTime());//这个就是把时间戳经过处理得到期望格
        String id = correlationData.getId();
        String jsonData = new String(correlationData.getReturnedMessage().getBody());
        RabbitMQRequestEntity rabbitMQRequest = JSONUtil.toBean(JSONUtil.toJsonStr(jsonData), RabbitMQRequestEntity.class);
        if (ack) {
            log.info("[MQProducerAckConfig.confirm]消息发送成功通道id[{}]时间[{}]  BODY={}", id, format, rabbitMQRequest);
        } else {
            log.error("[MQProducerAckConfig.confirm]消息发送失败通道id[{}]时间[{}] cause={}  BODY={}", id, format, cause, rabbitMQRequest);
        }
    }

    /**
     * Return 消息机制用于处理一个不可路由的消息。在某些情况下，如果我们在发送消息的时候，当前的 exchange 不存在或者指定路由 key 路由不到，这个时候我们需要监听这种不可达的消息
     * 就需要这种return机制
     *
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        RabbitMQRequestEntity rabbitMQRequest = JSONUtil.toBean(JSONUtil.toJsonStr(new String(message.getBody())), RabbitMQRequestEntity.class);
        SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = format0.format(new Date().getTime());//这个就是把时间戳经过处理得到期望格
        // 反序列化对象输出
        log.error("[MQProducerAckConfig.returnedMessage]消息送达MQ异常_业务id[{}]时间[{}]  \n 消息主体: {} \n 应答码: {} \n 描述: {} \n 消息使用的交换器: {} \n 消息使用的路由键: {}"
                , rabbitMQRequest.getBusinessId()
                , format
                , rabbitMQRequest.getData()
                , replyCode
                , replyText
                , exchange
                , routingKey);
    }
}
