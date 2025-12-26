package org.clf.springboot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    private final Logger logger = LoggerFactory.getLogger(RabbitMqConfig.class);
    // 1. 业务队列相关
    public static final String BUSINESS_QUEUE_NAME = "picture.queue"; // 业务队列（处理图片审核的队列）
    public static final String BUSINESS_EXCHANGE_NAME = "picture.exchange"; // 业务交换机
    public static final String BUSINESS_ROUTING_KEY = "picture.routing.key"; // 业务路由键

    // 2. 死信队列相关
    public static final String DEAD_LETTER_QUEUE_NAME = "picture.dlq.queue"; // 死信队列
    public static final String DEAD_LETTER_EXCHANGE_NAME = "picture.dlq.exchange"; // 死信交换机
    public static final String DEAD_LETTER_ROUTING_KEY = "picture.dlq.routing.key"; // 死信路由键

    public static final String RESULT_QUEUE_NAME = "picture.result.queue";
    public static final String RESULT_EXCHANGE_NAME = "picture.result.exchange";
    public static final String RESULT_ROUTING_KEY = "picture.result.routing.key";

    public static final String
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 声明死信交换机（普通交换机即可，类型Direct）
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DEAD_LETTER_EXCHANGE_NAME).durable(true).build();
    }

    // 声明死信队列
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE_NAME).build();
    }

    // 死信队列绑定死信交换机（指定死信路由键）
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    // 声明业务交换机
    @Bean
    public DirectExchange businessExchange() {
        return ExchangeBuilder.directExchange(BUSINESS_EXCHANGE_NAME).durable(true).build();
    }

    // 声明业务队列，并指定死信相关参数
    @Bean
    public Queue businessQueue() {
        Map<String, Object> args = new HashMap<>(3);
        // 绑定死信交换机
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE_NAME);
        // 绑定死信路由键
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        // 消息过期时间（可选，如10秒未处理则进入死信队列，单位：毫秒）
        args.put("x-message-ttl", 1800000);
        return QueueBuilder.durable(BUSINESS_QUEUE_NAME)
                .withArguments(args) // 关联死信配置
                .build();
    }

    // 业务队列绑定业务交换机（指定业务路由键）
    @Bean
    public Binding businessBinding() {
        return BindingBuilder.bind(businessQueue())
                .to(businessExchange())
                .with(BUSINESS_ROUTING_KEY);
    }

    @Bean
    public Queue resultQueue() {
        return QueueBuilder.durable(RESULT_QUEUE_NAME).build();
    }

    @Bean
    public Binding resultBinding() {
        return BindingBuilder.bind(resultQueue())
                .to(resultExchange())
                .with(RESULT_ROUTING_KEY);
    }

    @Bean
    public DirectExchange resultExchange() {
        return ExchangeBuilder.directExchange(RESULT_EXCHANGE_NAME).durable(true).build();
    }

    @Bean
    public RabbitTemplate.ConfirmCallback confirmCallback() {
        return (correlationData, ack, cause) -> {
            if (ack) {
                logger.info("成功送达业务队列");
            } else {
                logger.error("消息投送失败:" + cause);
            }
        };
    }

}