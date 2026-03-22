package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockUserContextServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-23T03:15:00Z"), ZoneOffset.UTC);
    private final MockUserContextService service = new MockUserContextService(fixedClock);

    @Test
    void getUserContext_shouldReturnOverdueScenario_forUser001() {
        UserContext context = service.getUserContext("user_001");
        LocalDate today = LocalDate.now(fixedClock);

        assertEquals("user_001", context.getUserId());
        assertEquals(today.minusDays(5), context.getDueDate());
        assertEquals(new BigDecimal("120000"), context.getOutstandingBalance());
        assertFalse(context.isHasPaymentToday());
        assertTrue(context.getRecentTransactions().isEmpty());
    }

    @Test
    void getUserContext_shouldReturnPaymentTodayScenario_forUser002_caseInsensitive() {
        UserContext context = service.getUserContext("USER_002");
        LocalDate today = LocalDate.now(fixedClock);

        assertEquals("user_002", context.getUserId());
        assertEquals(today.plusDays(5), context.getDueDate());
        assertEquals(new BigDecimal("80000"), context.getOutstandingBalance());
        assertTrue(context.isHasPaymentToday());
        assertTrue(context.getRecentTransactions().isEmpty());
    }

    @Test
    void getUserContext_shouldReturnDuplicateTransactionsScenario_forUser003() {
        UserContext context = service.getUserContext("user_003");
        LocalDate today = LocalDate.now(fixedClock);
        LocalDateTime now = LocalDateTime.now(fixedClock);

        assertEquals("user_003", context.getUserId());
        assertEquals(today.plusDays(5), context.getDueDate());
        assertEquals(new BigDecimal("45000"), context.getOutstandingBalance());
        assertFalse(context.isHasPaymentToday());

        List<Transaction> transactions = context.getRecentTransactions();
        assertEquals(3, transactions.size());
        assertEquals(new BigDecimal("2500"), transactions.get(0).getAmount());
        assertEquals(now.minusMinutes(3), transactions.get(0).getTimestamp());
        assertEquals(new BigDecimal("2500"), transactions.get(1).getAmount());
        assertEquals(now.minusMinutes(1), transactions.get(1).getTimestamp());
        assertEquals(new BigDecimal("1200"), transactions.get(2).getAmount());
        assertEquals(now.minusMinutes(20), transactions.get(2).getTimestamp());
    }

    @Test
    void getUserContext_shouldReturnDefaultScenario_forUnknownUser() {
        UserContext context = service.getUserContext("user_999");
        LocalDate today = LocalDate.now(fixedClock);

        assertEquals("user_999", context.getUserId());
        assertEquals(today.plusDays(10), context.getDueDate());
        assertEquals(new BigDecimal("0"), context.getOutstandingBalance());
        assertFalse(context.isHasPaymentToday());
        assertTrue(context.getRecentTransactions().isEmpty());
    }

    @Test
    void getUserContext_shouldHandleNullUserId_asDefaultScenario() {
        UserContext context = service.getUserContext(null);
        LocalDate today = LocalDate.now(fixedClock);

        assertEquals(null, context.getUserId());
        assertEquals(today.plusDays(10), context.getDueDate());
        assertEquals(new BigDecimal("0"), context.getOutstandingBalance());
        assertFalse(context.isHasPaymentToday());
        assertTrue(context.getRecentTransactions().isEmpty());
    }
}
