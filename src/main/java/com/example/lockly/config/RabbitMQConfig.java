package com.example.lockly.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "post.exchange";

    // Create post
    public static final String POST_NOTIFICATION_QUEUE = "post.notification.queue";
    public static final String POST_KEY = "post.create";

    // Detect nfws
    public static final String IMAGE_NSFW_QUEUE = "image.detect.nfws.queue";
    public static final String IMAGE_DETECT_KEY = "image.detect.nfws";

    // Queue
    @Bean
    public Queue queueNotificationFcm() {
        return new Queue(POST_NOTIFICATION_QUEUE, false);
    }

    @Bean
    public Queue queueDetectNfws() {
        return new Queue(IMAGE_NSFW_QUEUE, false);
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
