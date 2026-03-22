package com.pachara.somjeed.somjeed_chatbot.prediction;

import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.prediction.rule.PredictionRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultPredictionService implements PredictionService {

    private final Clock clock;
    private final java.util.List<PredictionRule> rules;

    @Override
    public Optional<PredictionResult> predict(UserContext context) {
        return rules.stream()
                .filter(rule -> rule.matches(context))
                .max(Comparator.comparingInt(PredictionRule::score))
                .map(rule -> toPredictionResult(rule.type(), context));
    }

    private PredictionResult toPredictionResult(PredictionType type, UserContext context) {
        if (PredictionType.OVERDUE.equals(type)) {
            long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(context.getDueDate(), LocalDate.now(clock));
            return new PredictionResult(
                    PredictionType.OVERDUE,
                    "Looks like your payment is overdue. Would you like to check your outstanding balance?",
                    "User due date is " + overdueDays + " days in the past",
                    0.95
            );
        }

        if (PredictionType.PAYMENT_CONFIRMED.equals(type)) {
            return new PredictionResult(
                    PredictionType.PAYMENT_CONFIRMED,
                    "Your payment was received today. Would you like to check your updated available credit?",
                    "User has a payment recorded today",
                    0.85
            );
        }

        String duplicateReason = context.getRecentTransactions().stream()
                .collect(Collectors.groupingBy(Transaction::getAmount, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() >= 2)
                .findFirst()
                .map(entry -> "Detected " + entry.getValue() + " similar transactions for amount " + entry.getKey())
                .orElse("Detected similar transactions within short time window");

        return new PredictionResult(
                PredictionType.DUPLICATE_TRANSACTION,
                "We detected similar transactions. Would you like to review them now?",
                duplicateReason,
                0.75
        );
    }
}
