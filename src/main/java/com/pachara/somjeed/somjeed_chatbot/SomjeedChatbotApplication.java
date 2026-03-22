package com.pachara.somjeed.somjeed_chatbot;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SomjeedChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SomjeedChatbotApplication.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
