package com.develop.mvp.pk.framework.mq.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * RabbitMQ 消息队列自动配置。
 *
 * <p>这是一个条件装配分支：只有运行时类路径存在 Spring AMQP 的
 * {@code org.springframework.amqp.rabbit.core.RabbitTemplate} 时，本配置才会生效。
 * 因此，未引入 RabbitMQ 相关依赖的应用不会被迫创建 RabbitMQ Bean，也不会改变 Redis MQ 的使用方式。</p>
 *
 * <p>当前配置只提供消息转换器这一基础能力，负责把消息体交给 Jackson 做 JSON 序列化与反序列化；
 * 交换机、队列、路由键以及业务消费规则仍应由业务模块或更上层配置声明。</p>
 *
 * @author David
 */
@AutoConfiguration
@Slf4j
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
public class DevelopRabbitMQAutoConfiguration {

    /**
     * 创建 RabbitMQ 消息转换器。
     *
     * <p>Spring AMQP 在发送和接收消息时会使用 {@link MessageConverter} 处理消息体格式。
     * 这里统一使用 {@link Jackson2JsonMessageConverter}，让 RabbitMQ 消息以 JSON 形式在 Java 对象和字节内容之间转换。</p>
     *
     * @return 基于 Jackson 的 RabbitMQ 消息转换器
     */
    @Bean
    public MessageConverter createMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
