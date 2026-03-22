package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultIntentHandlerTest {

    private final DefaultIntentHandler handler = new DefaultIntentHandler();

    @Test
    void handle_shouldMapCheckBalance() {
        ChatContext context = context();
        List<String> response = handler.handle(IntentType.CHECK_BALANCE, context);
        assertEquals(List.of("Your current outstanding balance is 120,000 THB."), response);
    }

    @Test
    void handle_shouldMapPaymentDue() {
        ChatContext context = context();
        List<String> response = handler.handle(IntentType.PAYMENT_DUE, context);
        assertEquals(List.of("Your payment is due on 1 September 2025."), response);
    }

    @Test
    void handle_shouldMapReportLostCard() {
        List<String> response = handler.handle(IntentType.REPORT_LOST_CARD, context());
        assertEquals(List.of("Please contact our support immediately to block your card."), response);
    }

    @Test
    void handle_shouldMapRewardPoints() {
        List<String> response = handler.handle(IntentType.REWARD_POINTS, context());
        assertEquals(List.of("You have 5,000 reward points available."), response);
    }

    @Test
    void handle_shouldMapClosing() {
        List<String> response = handler.handle(IntentType.CLOSING, context());
        assertEquals(List.of("You're welcome. Glad I could help. Have a great day."), response);
    }

    @Test
    void handle_shouldMapGeneralInquiry() {
        List<String> response = handler.handle(IntentType.GENERAL_INQUIRY, context());
        assertEquals(List.of("How can I assist you further?"), response);
    }

    @Test
    void handle_shouldFallbackToGeneralInquiry_whenIntentIsNull() {
        List<String> response = handler.handle(null, context());
        assertEquals(List.of("How can I assist you further?"), response);
    }

    @Test
    void handle_shouldReturnGeneralInquiry_whenIntentIsNullAndContextIsNull() {
        List<String> response = handler.handle(null, null);
        assertEquals(List.of("How can I assist you further?"), response);
    }

    @Test
    void handle_shouldThrow_whenCheckBalanceContextMissingOutstandingBalance() {
        ChatContext context = context();
        context.setOutstandingBalance(null);
        assertThrows(IllegalArgumentException.class, () -> handler.handle(IntentType.CHECK_BALANCE, context));
    }

    @Test
    void handle_shouldThrow_whenPaymentDueContextMissingDueDate() {
        ChatContext context = context();
        context.setDueDate(null);
        assertThrows(NullPointerException.class, () -> handler.handle(IntentType.PAYMENT_DUE, context));
    }

    @Test
    void handle_shouldThrow_whenRewardPointsContextMissingPoints() {
        ChatContext context = context();
        context.setRewardPoints(null);
        assertThrows(IllegalArgumentException.class, () -> handler.handle(IntentType.REWARD_POINTS, context));
    }

    @Test
    void handle_shouldThrow_whenNonFallbackIntentWithNullContext() {
        assertThrows(NullPointerException.class, () -> handler.handle(IntentType.CHECK_BALANCE, null));
    }

    private ChatContext context() {
        ChatContext context = new ChatContext();
        context.setDueDate(LocalDate.of(2025, 9, 1));
        context.setOutstandingBalance(new BigDecimal("120000"));
        context.setRewardPoints(5000);
        return context;
    }
}
