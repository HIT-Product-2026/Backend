package com.example.lockly.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String POST_NOTIFICATION_QUEUE = "post.notification.queue";
    public static final String EXCHANGE = "post.exchange";
    public static final String POST_KEY = "post.create";

    // Queue
    @Bean
    public Queue queue() {
        return new Queue(POST_NOTIFICATION_QUEUE, false);
    }

    // Exchange
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    // Binding (queue <-> exchange)
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(POST_KEY);
    }
}
