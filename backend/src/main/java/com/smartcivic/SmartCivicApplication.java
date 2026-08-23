package com.smartcivic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartCivicApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCivicApplication.class, args);
    }
}
