package com.example.lockly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // Dùng để delay thời gian
@SpringBootApplication
public class LocklyApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocklyApplication.class, args);
    }
}