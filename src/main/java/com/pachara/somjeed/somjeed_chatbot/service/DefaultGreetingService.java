package com.pachara.somjeed.somjeed_chatbot.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultGreetingService implements GreetingService {

    private final Clock clock;

    @Override
    public String generateGreeting() {
        LocalTime now = LocalDateTime.now(clock).toLocalTime();
        String greeting = resolveGreeting(now);
        String weather = resolveWeather(now);
        return greeting + ", " + weather;
    }

    private String resolveGreeting(LocalTime now) {
        if (!now.isBefore(LocalTime.of(5, 0)) && now.isBefore(LocalTime.NOON)) {
            return "Good morning";
        }
        if (!now.isBefore(LocalTime.NOON) && now.isBefore(LocalTime.of(17, 0))) {
            return "Good afternoon";
        }
        return "Good evening";
    }

    private String resolveWeather(LocalTime now) {
        return switch (now.getHour() % 4) {
            case 0 -> "on a sunshine day!";
            case 1 -> "stay dry out there!";
            case 2 -> "a bit cloudy but I’m here to help!";
            default -> "let me help make your stormy day better.";
        };
    }
}
