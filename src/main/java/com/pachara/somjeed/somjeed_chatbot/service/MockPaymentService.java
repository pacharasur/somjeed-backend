package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;
import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class MockPaymentService implements PaymentService {

    private final Clock clock;
    private ChatContext chatContext;

    @Override
    public ChatContext getChatContext() {
        if (chatContext == null || !chatContext.isAwaitingConfirmation()) {
            chatContext = selectRandomScenario();
        }
        return chatContext;
    }

    private ChatContext selectRandomScenario() {
        int scenario = ThreadLocalRandom.current().nextInt(3);
        LocalDate today = LocalDate.now(clock);

        if (scenario == 0) {
            log.info("MockPaymentService selected scenario: OVERDUE");
            return new ChatContext(
                    today.minusDays(1),
                    new BigDecimal("120000"),
                    5000,
                    null,
                    Collections.emptyList(),
                    null,
                    false,
                    false
            );
        }

        if (scenario == 1) {
            log.info("MockPaymentService selected scenario: PAYMENT_CONFIRMED");
            return new ChatContext(
                    today.plusDays(5),
                    new BigDecimal("120000"),
                    5000,
                    today,
                    Collections.emptyList(),
                    null,
                    false,
                    false
            );
        }

        log.info("MockPaymentService selected scenario: DUPLICATE_TRANSACTION");
        return new ChatContext(
                today.plusDays(5),
                new BigDecimal("120000"),
                5000,
                null,
                duplicateTransactions(today),
                null,
                false,
                false
        );
    }

    private List<Transaction> duplicateTransactions(LocalDate today) {
        return List.of(
                new Transaction(new BigDecimal("2500"), LocalDateTime.of(today.minusDays(1), java.time.LocalTime.of(10, 0))),
                new Transaction(new BigDecimal("2500"), LocalDateTime.of(today.minusDays(1), java.time.LocalTime.of(10, 2)))
        );
    }
}
