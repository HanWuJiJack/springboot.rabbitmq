package com.shiguangping.mqprovider.controller;

import cn.hutool.json.JSONUtil;
import com.shiguangping.mqprovider.alternateexchange.config.RabbitAlternateUtils;
import com.shiguangping.mqprovider.deadmsg.config.RabbitDeadUtils;
import com.shiguangping.mqprovider.delayedExchange.config.RabbitDelayedUtils;
import com.shiguangping.mqprovider.entity.RabbitMQRequestEntity;
import com.shiguangping.mqprovider.entity.User;
import com.shiguangping.mqprovider.entityExchange.config.RabbitEntityUtils;
import com.shiguangping.mqprovider.priorityExchange.config.RabbitPriorityUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author liyan
 */

@RestController
@RequestMapping("/mq")
public class TestController {
    @Autowired
    private RabbitEntityUtils rabbitEntityUtils;
    //    备份
    @Autowired
    private RabbitAlternateUtils rabbitTemplateAlternate;

    //    死信
    @Autowired
    private RabbitDeadUtils rabbitTemplateDead;

    //    延迟
    @Autowired
    private RabbitDelayedUtils rabbitDelayedUtils;

//    权重队列
    @Autowired
    private RabbitPriorityUtils rabbitPriorityUtils;

    @GetMapping("/send/{msg}")
    public void test(@PathVariable("msg") String msg) {
        //        死信队列
        //        rabbitTemplateDead.sendMsgTimeToLive(msg);
        //        备份队列
        //        rabbitTemplateAlternate.sendMsg(msg);
        //        RabbitMQ延迟消息基于插件x-delayed-message

//        rabbitDelayedUtils.sendMsgTimeToLive(msg);

//        rabbitTemplateDead.sendMsgReturn(msg);
//        Integer[] ints = { 1 ,2,3,1,4,4};
//        for (int num: ints) {
//            rabbitPriorityUtils.sendPriorityQueue(String.valueOf(num), num);
//        }

//        java.util.Random random = new java.util.Random(); // 定义随机类
//        Integer result = random.nextInt(10); // 返回[0,10)集合中的整数，注意不包括10
//        rabbitTemplateDead.sendPriorityQueue(msg, result);
//        rabbitTemplateDead.sendMsg(msg);

        rabbitEntityUtils.send();
    }
}
