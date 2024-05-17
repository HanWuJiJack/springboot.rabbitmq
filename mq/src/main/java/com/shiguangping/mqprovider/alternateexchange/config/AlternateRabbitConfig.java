package com.shiguangping.mqprovider.alternateexchange.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootConfiguration
public class AlternateRabbitConfig {

    public static final String EXCHANGE_A = "direct.exchange.test.ae-normal";
    public static final String EXCHANGE_alternate = "fanout.exchange.test.ae";

    public static final String QUEUE_A = "direct.queue.test.ae-normal";
    public static final String QUEUE_alternate = "fanout.queue.test.ae";

    public static final String ROUTINGKEY_A = "direct.routing-key.test.ae-normal";

    /**
     * 　a.备份交换机（alternate-exchange）：备份交换器是为了实现没有路由到队列的消息，
     * 声明交换机的时候添加属性alternate-exchange，声明一个备用交换机，一般声明为fanout类型，
     * 这样交换机收到路由不到队列的消息就会发送到备用交换机绑定的队列中。
     *
     * 　b.死信队列（dead-letter-exchange）：当消息在一个队列中变成死信 (dead message) 之后，
     * 它能被重新publish到另一个Exchange，这个Exchange就是DLX。
     */


// 主要交换机
    @Bean
    public DirectExchange AlternateExchangeNormal() {
        Map<String, Object> args = new HashMap<>(4);

//        alternate-exchange
//        下面简称AE，当一个消息不能被route的时候，如果exchange设定了AE，则消息会被投递到AE。
//        如果存在AE链，则会按此继续投递，直到消息被route或AE链结束或遇到已经尝试route过消息的AE。

        args.put("alternate-exchange", EXCHANGE_alternate); //绑定备份交换机
        return new DirectExchange(EXCHANGE_A, true, false, args);
//        return new DirectExchange(EXCHANGE_A);
    }

    /**
     * 获取队列
     * @return
     */
    @Bean
    public Queue AlternateQueueNormal() {
        Queue queue = new Queue(QUEUE_A, true);
        return queue;
    }

    /**
     * 把交换机，队列，通过路由关键字进行绑定
     * @return
     */
    @Bean
    public Binding AlternatequeueBindingExchangeNormal(){
        return BindingBuilder.bind(AlternateQueueNormal()).to(AlternateExchangeNormal()).with(ROUTINGKEY_A);
    }
    //  备份交换机
    @Bean
    public FanoutExchange AlternateExchange() {
        return new FanoutExchange(EXCHANGE_alternate,true,false);
    }


    /**
     * 备份队列
     * @return
     */
    @Bean
    public Queue AlternateQueue() {
        Queue queue = new Queue(QUEUE_alternate, true);
        return queue;
    }




    /**
     * 把交换机，队列，通过路由关键字进行绑定
     * @return
     */
    @Bean
    public Binding AlternatequeueBindingExchange(){
        return BindingBuilder.bind(AlternateQueue()).to(AlternateExchange());
    }



}
