package com.pachara.somjeed.somjeed_chatbot.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultGreetingServiceTest {

    @Test
    void generateGreeting_shouldReturnMorningGreeting() {
        DefaultGreetingService service = serviceAt("2026-03-23T05:00:00Z");

        String greeting = service.generateGreeting();

        assertEquals("Good morning, stay dry out there!", greeting);
    }

    @Test
    void generateGreeting_shouldReturnAfternoonGreeting() {
        DefaultGreetingService service = serviceAt("2026-03-23T12:00:00Z");

        String greeting = service.generateGreeting();

        assertEquals("Good afternoon, on a sunshine day!", greeting);
    }

    @Test
    void generateGreeting_shouldReturnEveningGreeting() {
        DefaultGreetingService service = serviceAt("2026-03-23T17:00:00Z");

        String greeting = service.generateGreeting();

        assertEquals("Good evening, stay dry out there!", greeting);
    }

    @Test
    void generateGreeting_shouldReturnSunshineWeatherCondition() {
        DefaultGreetingService service = serviceAt("2026-03-23T08:00:00Z");

        String greeting = service.generateGreeting();

        assertEquals("Good morning, on a sunshine day!", greeting);
    }

    @Test
    void generateGreeting_shouldReturnRainyWeatherCondition() {
        DefaultGreetingService service = serviceAt("2026-03-23T09:00:00Z");

        String greeting = service.generateGreeting();

        assertEquals("Good morning, stay dry out there!", greeting);
    }

    @Test
    void generateGreeting_shouldReturnCloudyWeatherCondition() {
        DefaultGreetingService service = serviceAt("2026-03-23T10:00:00Z");

        String greeting = service.generateGreeting();

        assertEquals("Good morning, a bit cloudy but I’m here to help!", greeting);
    }

    @Test
    void generateGreeting_shouldReturnStormyWeatherCondition() {
        DefaultGreetingService service = serviceAt("2026-03-23T11:00:00Z");

        String greeting = service.generateGreeting();

        assertEquals("Good morning, let me help make your stormy day better.", greeting);
    }

    private DefaultGreetingService serviceAt(String instant) {
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
        return new DefaultGreetingService(clock);
    }
}
