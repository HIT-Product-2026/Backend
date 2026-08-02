package com.example.lockly.service.Impl;

import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;

public class ScheduledExecutorService {
    @Bean
    public java.util.concurrent.ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(2);
    }
}
