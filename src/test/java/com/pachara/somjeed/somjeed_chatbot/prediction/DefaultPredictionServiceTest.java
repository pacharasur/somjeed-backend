package com.pachara.somjeed.somjeed_chatbot.prediction;

import com.pachara.somjeed.somjeed_chatbot.enums.PredictionTypeEnum;
import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.prediction.rule.DuplicateTransactionRule;
import com.pachara.somjeed.somjeed_chatbot.prediction.rule.OverdueRule;
import com.pachara.somjeed.somjeed_chatbot.prediction.rule.PaymentTodayRule;
import com.pachara.somjeed.somjeed_chatbot.prediction.rule.PredictionRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPredictionServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-23T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private PredictionRule mockRule;

    @Test
    void predict_shouldReturnEmpty_whenNoRuleMatches() {
        DefaultPredictionService service = new DefaultPredictionService(
                fixedClock,
                List.of(new OverdueRule(fixedClock), new PaymentTodayRule(), new DuplicateTransactionRule())
        );
        UserContext context = userContext("u1", LocalDate.of(2026, 3, 25), false, List.of());

        Optional<PredictionResult> result = service.predict(context);

        assertTrue(result.isEmpty());
    }

    @Test
    void predict_shouldSelectHighestScore_whenMultipleRulesMatch() {
        DefaultPredictionService service = new DefaultPredictionService(
                fixedClock,
                List.of(new PaymentTodayRule(), new DuplicateTransactionRule(), new OverdueRule(fixedClock))
        );
        LocalDateTime t1 = LocalDateTime.of(2026, 3, 22, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 3, 22, 10, 5);
        UserContext context = userContext(
                "u1",
                LocalDate.of(2026, 3, 20),
                true,
                List.of(new Transaction(new BigDecimal("500"), t1), new Transaction(new BigDecimal("500"), t2))
        );

        PredictionResult result = service.predict(context).orElseThrow();

        assertEquals(PredictionTypeEnum.OVERDUE, result.predictionType());
    }

    @Test
    void predict_shouldReturnPaymentConfirmed_whenPaymentTodayMatches() {
        DefaultPredictionService service = new DefaultPredictionService(
                fixedClock,
                List.of(new PaymentTodayRule())
        );
        UserContext context = userContext("u2", LocalDate.of(2026, 3, 25), true, List.of());

        PredictionResult result = service.predict(context).orElseThrow();

        assertEquals(PredictionTypeEnum.PAYMENT_CONFIRMED, result.predictionType());
        assertEquals(
                "Your payment was received today. Would you like to check your updated available credit?",
                result.message()
        );
    }

    @Test
    void predict_shouldReturnDuplicateReasonWithAmount_whenDuplicateMatches() {
        DefaultPredictionService service = new DefaultPredictionService(
                fixedClock,
                List.of(new DuplicateTransactionRule())
        );
        LocalDateTime t1 = LocalDateTime.of(2026, 3, 23, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 3, 23, 10, 3);
        UserContext context = userContext(
                "u3",
                LocalDate.of(2026, 3, 30),
                false,
                List.of(new Transaction(new BigDecimal("999"), t1), new Transaction(new BigDecimal("999"), t2))
        );

        PredictionResult result = service.predict(context).orElseThrow();

        assertEquals(PredictionTypeEnum.DUPLICATE_TRANSACTION, result.predictionType());
        assertEquals("We detected similar transactions. Would you like to review them now?", result.message());
    }

    @Test
    void predict_shouldThrowNpe_whenMatchedRuleTypeIsNull() {
        DefaultPredictionService service = new DefaultPredictionService(fixedClock, List.of(mockRule));
        UserContext context = userContext("u5", LocalDate.of(2026, 3, 30), false, List.of());

        when(mockRule.matches(context)).thenReturn(true);
        when(mockRule.type()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> service.predict(context));
    }

    @Test
    void predict_shouldReturnEmpty_whenRulesListIsEmpty() {
        DefaultPredictionService service = new DefaultPredictionService(fixedClock, List.of());
        UserContext context = userContext("u6", LocalDate.of(2026, 3, 1), true, List.of());

        Optional<PredictionResult> result = service.predict(context);

        assertFalse(result.isPresent());
    }

    private UserContext userContext(
            String userId,
            LocalDate dueDate,
            boolean hasPaymentToday,
            List<Transaction> recentTransactions
    ) {
        return new UserContext(userId, dueDate, new BigDecimal("120000"), hasPaymentToday, recentTransactions);
    }
}
