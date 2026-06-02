package com.flash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InterviewFlashApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewFlashApplication.class, args);
    }
}
