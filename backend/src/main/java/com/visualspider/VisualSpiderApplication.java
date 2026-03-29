package com.visualspider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VisualSpiderApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisualSpiderApplication.class, args);
    }

}
