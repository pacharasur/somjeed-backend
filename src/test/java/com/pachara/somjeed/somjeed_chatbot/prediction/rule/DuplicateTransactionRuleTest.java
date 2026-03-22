package com.pachara.somjeed.somjeed_chatbot.prediction.rule;

import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DuplicateTransactionRuleTest {

    private final DuplicateTransactionRule rule = new DuplicateTransactionRule();

    @Test
    void matches_shouldReturnFalse_whenTransactionsNull() {
        UserContext context = new UserContext("u1", LocalDate.now(), BigDecimal.ZERO, false, null);
        assertFalse(rule.matches(context));
    }

    @Test
    void matches_shouldReturnFalse_whenOnlyOneTransaction() {
        LocalDateTime t1 = LocalDateTime.of(2026, 3, 23, 10, 0);
        UserContext context = new UserContext(
                "u1",
                LocalDate.now(),
                BigDecimal.ZERO,
                false,
                List.of(new Transaction(new BigDecimal("100"), t1))
        );
        assertFalse(rule.matches(context));
    }

    @Test
    void matches_shouldReturnTrue_whenSameAmountWithinWindow() {
        LocalDateTime t1 = LocalDateTime.of(2026, 3, 23, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 3, 23, 10, 9);
        UserContext context = new UserContext(
                "u1",
                LocalDate.now(),
                BigDecimal.ZERO,
                false,
                List.of(new Transaction(new BigDecimal("100"), t1), new Transaction(new BigDecimal("100"), t2))
        );
        assertTrue(rule.matches(context));
    }

    @Test
    void matches_shouldReturnTrue_whenListIsUnsortedButDuplicateExists() {
        LocalDateTime t1 = LocalDateTime.of(2026, 3, 23, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 3, 23, 10, 5);
        UserContext context = new UserContext(
                "u1",
                LocalDate.now(),
                BigDecimal.ZERO,
                false,
                List.of(new Transaction(new BigDecimal("200"), t2), new Transaction(new BigDecimal("200"), t1))
        );
        assertTrue(rule.matches(context));
    }

    @Test
    void matches_shouldReturnFalse_whenSameAmountOutsideWindow() {
        LocalDateTime t1 = LocalDateTime.of(2026, 3, 23, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 3, 23, 10, 11);
        UserContext context = new UserContext(
                "u1",
                LocalDate.now(),
                BigDecimal.ZERO,
                false,
                List.of(new Transaction(new BigDecimal("100"), t1), new Transaction(new BigDecimal("100"), t2))
        );
        assertFalse(rule.matches(context));
    }

    @Test
    void matches_shouldReturnFalse_whenDifferentAmountWithinWindow() {
        LocalDateTime t1 = LocalDateTime.of(2026, 3, 23, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 3, 23, 10, 5);
        UserContext context = new UserContext(
                "u1",
                LocalDate.now(),
                BigDecimal.ZERO,
                false,
                List.of(new Transaction(new BigDecimal("100"), t1), new Transaction(new BigDecimal("120"), t2))
        );
        assertFalse(rule.matches(context));
    }
}
