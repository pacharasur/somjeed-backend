package com.pachara.somjeed.somjeed_chatbot.model.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentSummary(
        LocalDate dueDate,
        BigDecimal outstandingBalance,
        LocalDate lastPaymentDate
) {
}
