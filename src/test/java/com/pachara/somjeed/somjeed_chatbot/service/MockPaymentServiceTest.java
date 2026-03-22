package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;
import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockPaymentServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-23T03:15:00Z"), ZoneOffset.UTC);

    @Test
    void getChatContext_shouldCreateScenarioWithExpectedDefaults_whenNoExistingContext() {
        MockPaymentService service = new MockPaymentService(fixedClock);
        LocalDate today = LocalDate.now(fixedClock);

        ChatContext context = service.getChatContext();

        assertNotNull(context);
        assertEquals(new BigDecimal("120000"), context.getOutstandingBalance());
        assertEquals(5000, context.getRewardPoints());
        assertFalse(context.isAwaitingConfirmation());
        assertFalse(context.isAwaitingCancellation());
        assertTrue(context.getDueDate().equals(today.minusDays(1)) || context.getDueDate().equals(today.plusDays(5)));

        if (context.getLastPaymentDate() != null) {
            assertEquals(today, context.getLastPaymentDate());
            assertTrue(context.getTransactions().isEmpty());
        } else if (!context.getTransactions().isEmpty()) {
            assertEquals(2, context.getTransactions().size());
            assertEquals(new BigDecimal("2500"), context.getTransactions().get(0).getAmount());
            assertEquals(new BigDecimal("2500"), context.getTransactions().get(1).getAmount());
        } else {
            assertTrue(context.getTransactions().isEmpty());
        }
    }

    @Test
    void getChatContext_shouldReuseStoredContext_whenAwaitingConfirmationIsTrue() {
        MockPaymentService service = new MockPaymentService(fixedClock);
        ChatContext existing = new ChatContext(
                LocalDate.of(2026, 3, 20),
                new BigDecimal("999"),
                111,
                null,
                Collections.emptyList(),
                null,
                true,
                false
        );
        ReflectionTestUtils.setField(service, "chatContext", existing);

        ChatContext result = service.getChatContext();

        assertSame(existing, result);
        assertEquals(new BigDecimal("999"), result.getOutstandingBalance());
    }

    @Test
    void getChatContext_shouldReplaceStoredContext_whenAwaitingConfirmationIsFalse() {
        MockPaymentService service = new MockPaymentService(fixedClock);
        ChatContext existing = new ChatContext(
                LocalDate.of(2026, 3, 20),
                new BigDecimal("999"),
                111,
                null,
                Collections.emptyList(),
                null,
                false,
                false
        );
        ReflectionTestUtils.setField(service, "chatContext", existing);

        ChatContext result = service.getChatContext();

        assertNotNull(result);
        assertTrue(result != existing);
        assertEquals(new BigDecimal("120000"), result.getOutstandingBalance());
        assertEquals(5000, result.getRewardPoints());
    }

    @Test
    void getChatContext_shouldReturnOverdueScenario_whenPreloaded() {
        MockPaymentService service = new MockPaymentService(fixedClock);
        ChatContext overdue = new ChatContext(
                LocalDate.of(2026, 3, 22),
                new BigDecimal("120000"),
                5000,
                null,
                Collections.emptyList(),
                null,
                true,
                false
        );
        ReflectionTestUtils.setField(service, "chatContext", overdue);

        ChatContext result = service.getChatContext();

        assertSame(overdue, result);
        assertNull(result.getLastPaymentDate());
        assertTrue(result.getTransactions().isEmpty());
    }

    @Test
    void getChatContext_shouldReturnPaymentConfirmedScenario_whenPreloaded() {
        MockPaymentService service = new MockPaymentService(fixedClock);
        ChatContext paymentConfirmed = new ChatContext(
                LocalDate.of(2026, 3, 28),
                new BigDecimal("120000"),
                5000,
                LocalDate.of(2026, 3, 23),
                Collections.emptyList(),
                null,
                true,
                false
        );
        ReflectionTestUtils.setField(service, "chatContext", paymentConfirmed);

        ChatContext result = service.getChatContext();

        assertSame(paymentConfirmed, result);
        assertEquals(LocalDate.of(2026, 3, 23), result.getLastPaymentDate());
        assertTrue(result.getTransactions().isEmpty());
    }

    @Test
    void getChatContext_shouldReturnDuplicateTransactionScenario_whenPreloaded() {
        MockPaymentService service = new MockPaymentService(fixedClock);
        List<Transaction> transactions = List.of(
                new Transaction(new BigDecimal("2500"), LocalDateTime.of(2026, 3, 22, 10, 0)),
                new Transaction(new BigDecimal("2500"), LocalDateTime.of(2026, 3, 22, 10, 2))
        );
        ChatContext duplicateTx = new ChatContext(
                LocalDate.of(2026, 3, 28),
                new BigDecimal("120000"),
                5000,
                null,
                transactions,
                null,
                true,
                false
        );
        ReflectionTestUtils.setField(service, "chatContext", duplicateTx);

        ChatContext result = service.getChatContext();

        assertSame(duplicateTx, result);
        assertEquals(2, result.getTransactions().size());
        assertEquals(new BigDecimal("2500"), result.getTransactions().get(0).getAmount());
        assertEquals(new BigDecimal("2500"), result.getTransactions().get(1).getAmount());
    }

    @Test
    void getChatContext_shouldThrowException_whenClockIsNull() {
        MockPaymentService service = new MockPaymentService(null);

        assertThrows(NullPointerException.class, service::getChatContext);
    }

    @Test
    void getChatContext_shouldReturnInvalidStoredContextAsIs_whenAwaitingConfirmationTrue() {
        MockPaymentService service = new MockPaymentService(fixedClock);
        ChatContext invalid = new ChatContext(
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                false
        );
        ReflectionTestUtils.setField(service, "chatContext", invalid);

        ChatContext result = service.getChatContext();

        assertSame(invalid, result);
        assertNull(result.getDueDate());
        assertNull(result.getOutstandingBalance());
        assertNull(result.getTransactions());
    }
}
