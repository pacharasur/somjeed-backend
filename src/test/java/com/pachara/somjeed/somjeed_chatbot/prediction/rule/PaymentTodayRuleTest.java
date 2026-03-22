package com.pachara.somjeed.somjeed_chatbot.prediction.rule;

import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTodayRuleTest {

    private final PaymentTodayRule rule = new PaymentTodayRule();

    @Test
    void matches_shouldReturnTrue_whenHasPaymentToday() {
        UserContext context = new UserContext("u1", LocalDate.now(), BigDecimal.TEN, true, List.of());
        assertTrue(rule.matches(context));
    }

    @Test
    void matches_shouldReturnFalse_whenNoPaymentToday() {
        UserContext context = new UserContext("u1", LocalDate.now(), BigDecimal.TEN, false, List.of());
        assertFalse(rule.matches(context));
    }
}
