package com.ai.platform.logaiplatform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class LogAiPlatformApplication {
    public static void main(String[] args) {

        SpringApplication.run(LogAiPlatformApplication.class, args);
    }

}