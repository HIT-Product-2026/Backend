package com.example.lockly;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.parameters.P;

@SpringBootApplication
public class LocklyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocklyApplication.class, args);
	}

}
