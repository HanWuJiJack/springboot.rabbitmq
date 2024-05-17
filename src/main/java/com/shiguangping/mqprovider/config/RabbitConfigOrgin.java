//package com.shiguangping.mqprovider.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.AcknowledgeMode;
//import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
//import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.SpringBootConfiguration;
//import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.retry.RetryCallback;
//import org.springframework.retry.RetryContext;
//import org.springframework.retry.RetryListener;
//import org.springframework.retry.backoff.ExponentialBackOffPolicy;
//import org.springframework.retry.policy.SimpleRetryPolicy;
//import org.springframework.retry.support.RetryTemplate;
//
////常用的三个配置如下
////1---设置手动应答（acknowledge-mode: manual）
//// 2---设置生产者消息发送的确认回调机制 (  #这个配置是保证提供者确保消息推送到交换机中，不管成不成功，都会回调
////    publisher-confirm-type: correlated
////    #保证交换机能把消息推送到队列中
////    publisher-returns: true
////     template:
////      #以下是rabbitmqTemplate配置
////      mandatory: true)
//// 3---设置重试
//@Slf4j
//@SpringBootConfiguration
//public class RabbitConfigOrgin {
//
//    @Value("${spring.rabbitmq.host}")
//    private String host;
//
//    @Value("${spring.rabbitmq.port}")
//    private int port;
//
//    @Value("${spring.rabbitmq.username}")
//    private String username;
//
//    @Value("${spring.rabbitmq.password}")
//    private String password;
//
//    @Autowired
//    private RabbitProperties properties;
//
//    //这里因为使用自动配置的connectionFactory，所以把自定义的connectionFactory注解掉
//    // 存在此名字的bean 自带的连接工厂会不加载（也就是说yml中rabbitmq下一级不生效），如果想自定义来区分开 需要改变bean 的名称
//    @Bean
//    public CachingConnectionFactory connectionFactory() {
//        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
//        connectionFactory.setUsername(username);
//        connectionFactory.setPassword(password);
//        connectionFactory.setVirtualHost("/");
//
//        // 确认开启ConfirmCallback回调
//
//        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
//
//        //设置发布消息后回调
//        connectionFactory.setPublisherReturns(true);
//        //设置发布后确认类型，此处确认类型为交互
//        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
//
//        connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
//        return connectionFactory;
//    }
//
//
//    // 存在此名字的bean 自带的容器工厂会不加载（yml下rabbitmq下的listener下的simple配置），如果想自定义来区分开 需要改变bean 的名称
//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
//        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
//        containerFactory.setConnectionFactory(connectionFactory());
//
//        // 并发消费者数量
////        # 消费端的监听个数(即@RabbitListener开启几个线程去处理数据。)
//        containerFactory.setConcurrentConsumers(1);
////         # 消费端的监听最大个数
//        containerFactory.setMaxConcurrentConsumers(20);
//
//        //    不公平分发
////        每个customer会在MQ预取一些消息放入内存的LinkedBlockingQueue中，这个值越高，
////        消息传递的越快，但非顺序处理消息的风险更高。如果ack模式为none，则忽略。如有必要，
////        将增加此值以匹配txSize或messagePerAck。从2.0开始默认为250；
////        设置为1将还原为以前的行为。
//        containerFactory.setPrefetchCount(1);
//
//        // 应答模式（此处设置为手动）
//        containerFactory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//
//        //消息序列化方式
//        containerFactory.setMessageConverter(new Jackson2JsonMessageConverter());
//        // 设置通知调用链 （这里设置的是重试机制的调用链）
//        containerFactory.setAdviceChain(
//                RetryInterceptorBuilder
//                        .stateless()
//                        .recoverer(new RejectAndDontRequeueRecoverer())
//                        .retryOperations(rabbitRetryTemplate())
//                        .build()
//        );
//        return containerFactory;
//    }
//
//    // 存在此名字的bean 自带的容器工厂会不加载（yml下rabbitmq下的template的配置），如果想自定义来区分开 需要改变bean 的名称
//    @Bean
//    public RabbitTemplate rabbitTemplate() {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
//        //默认是用jdk序列化
//        //数据转换为json存入消息队列，方便可视化界面查看消息数据
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
//        //设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
//        rabbitTemplate.setMandatory(true);
//        //此处设置重试template后，会再生产者发送消息的时候，调用该template中的调用链
//        rabbitTemplate.setRetryTemplate(rabbitRetryTemplate());
//        //CorrelationData correlationData, boolean ack, String cause
//        rabbitTemplate.setConfirmCallback(
//                (correlationData, ack, cause) -> {
//                    log.info("确认放入交换机回调id:" + correlationData);
//                    if (ack) {
//                        log.info("确认放入交换机回调入队成功");
//                    } else {
//                        log.info("确认放入交换机回调入队失败:" + cause);
//                    }
//
//                });
//        //Message message, int i, String s, String s1, String s2
//        rabbitTemplate.setReturnCallback((message, i, s, s1, s2) -> {
//            log.error("消息{},被交换机{}退回,退回的原因:{},路由key是:{}", message, s1, s, s2);
////            System.out.println("ReturnCallback：     " + "消息：" + message);
////            System.out.println("ReturnCallback：     " + "回应码：" + i);
////            System.out.println("ReturnCallback：     " + "回应消息：" + s);
////            System.out.println("ReturnCallback：     " + "交换机：" + s1);
////            System.out.println("ReturnCallback：     " + "路由键：" + s2);
//        });
//        return rabbitTemplate;
//    }
//
//    //重试的Template
//    @Bean
//    public RetryTemplate rabbitRetryTemplate() {
//        RetryTemplate retryTemplate = new RetryTemplate();
//        // 设置监听  调用重试处理过程
//        retryTemplate.registerListener(new RetryListener() {
//            @Override
//            public <T, E extends Throwable> boolean open(RetryContext retryContext, RetryCallback<T, E> retryCallback) {
//                // 执行之前调用 （返回false时会终止执行）
//                return true;
//            }
//
//            @Override
//            public <T, E extends Throwable> void close(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {
//                // 重试结束的时候调用 （最后一次重试 ）
//                System.out.println("---------------最后一次调用");
//
//                return;
//            }
//
//            @Override
//            public <T, E extends Throwable> void onError(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {
//                //  异常 都会调用
//                System.err.println("-----第{}次调用" + retryContext.getRetryCount());
//            }
//        });
//        retryTemplate.setBackOffPolicy(backOffPolicyByProperties());
//        retryTemplate.setRetryPolicy(retryPolicyByProperties());
//        return retryTemplate;
//    }
//
//    @Bean
//    public ExponentialBackOffPolicy backOffPolicyByProperties() {
//        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
//        long maxInterval = properties.getListener().getSimple().getRetry().getMaxInterval().getSeconds();
//        long initialInterval = properties.getListener().getSimple().getRetry().getInitialInterval().getSeconds();
//        double multiplier = properties.getListener().getSimple().getRetry().getMultiplier();
//        // 重试间隔
//        backOffPolicy.setInitialInterval(initialInterval * 1000);
//        // 重试最大间隔
//        backOffPolicy.setMaxInterval(maxInterval * 1000);
//        // 重试间隔乘法策略
//        backOffPolicy.setMultiplier(multiplier);
//        return backOffPolicy;
//    }
//
//    @Bean
//    public SimpleRetryPolicy retryPolicyByProperties() {
//        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
//        int maxAttempts = properties.getListener().getSimple().getRetry().getMaxAttempts();
//        retryPolicy.setMaxAttempts(maxAttempts);
//        return retryPolicy;
//    }
//}