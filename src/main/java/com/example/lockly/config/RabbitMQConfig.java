package com.example.lockly.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "post.exchange";

    // Create post
    public static final String POST_NOTIFICATION_QUEUE = "post.notification.queue";
    public static final String POST_KEY = "post.create";

    // Detect nfws
    public static final String IMAGE_NSFW_QUEUE = "image.detect.nfws.queue";
    public static final String IMAGE_DETECT_KEY = "image.detect.nfws";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        return rabbitTemplate;
    }

    // Queue
    @Bean
    public Queue queueNotificationFcm() {
        return new Queue(POST_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Queue queueDetectNfws() {
        return new Queue(IMAGE_NSFW_QUEUE, true);
    }

    // Exchange
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    // Binding (queue <-> exchange)
    @Bean
    public Binding bindingNotificationFcm(Queue queueNotificationFcm, DirectExchange exchange) {
        return BindingBuilder
                .bind(queueNotificationFcm)
                .to(exchange)
                .with(POST_KEY);
    }

    @Bean
    public Binding bindingDetectNfws(Queue queueDetectNfws, DirectExchange exchange) {
        return BindingBuilder
                .bind(queueDetectNfws)
                .to(exchange)
                .with(IMAGE_DETECT_KEY);
    }
}
