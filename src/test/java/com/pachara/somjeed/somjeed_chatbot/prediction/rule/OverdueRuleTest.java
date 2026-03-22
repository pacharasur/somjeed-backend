package com.pachara.somjeed.somjeed_chatbot.prediction.rule;

import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverdueRuleTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-23T00:00:00Z"), ZoneOffset.UTC);
    private final OverdueRule rule = new OverdueRule(fixedClock);

    @Test
    void matches_shouldReturnTrue_whenDueDateInPast() {
        UserContext context = new UserContext("u1", LocalDate.of(2026, 3, 22), BigDecimal.ONE, false, List.of());
        assertTrue(rule.matches(context));
    }

    @Test
    void matches_shouldReturnFalse_whenDueDateToday() {
        UserContext context = new UserContext("u1", LocalDate.of(2026, 3, 23), BigDecimal.ONE, false, List.of());
        assertFalse(rule.matches(context));
    }

    @Test
    void matches_shouldReturnFalse_whenDueDateNull() {
        UserContext context = new UserContext("u1", null, BigDecimal.ONE, false, List.of());
        assertFalse(rule.matches(context));
    }
}
