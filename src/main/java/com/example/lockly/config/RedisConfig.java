package com.example.lockly.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory){

        RedisTemplate<String,Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(factory);

        log.debug("Connect to Redis");
        return template;
    }

}
