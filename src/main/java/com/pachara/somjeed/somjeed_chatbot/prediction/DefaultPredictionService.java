package com.pachara.somjeed.somjeed_chatbot.prediction;

import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.prediction.rule.PredictionRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultPredictionService implements PredictionService {

    private final Clock clock;
    private final List<PredictionRule> rules;

    @Override
    public Optional<PredictionResult> predict(UserContext context) {
        return rules.stream()
                .filter(rule -> rule.matches(context))
                .max(Comparator.comparingInt(PredictionRule::score))
                .map(rule -> toPredictionResult(rule.type(), context));
    }

    private PredictionResult toPredictionResult(PredictionType type, UserContext context) {
        return Optional.ofNullable(handlers.get(type))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported prediction type: " + type))
                .apply(context);
    }

    private final Map<PredictionType, Function<UserContext, PredictionResult>> handlers = Map.of(
            PredictionType.OVERDUE, this::buildOverdue,
            PredictionType.PAYMENT_CONFIRMED, this::buildPaymentConfirmed,
            PredictionType.DUPLICATE_TRANSACTION, this::buildDuplicate
    );

    private PredictionResult buildOverdue(UserContext context) {
        long overdueDays = ChronoUnit.DAYS.between(context.getDueDate(), LocalDate.now(clock));

        return new PredictionResult(
                PredictionType.OVERDUE,
                "Looks like your payment is overdue. Would you like to check your outstanding balance?",
                "User due date is " + overdueDays + " days in the past",
                0.95
        );
    }

    private PredictionResult buildPaymentConfirmed(UserContext context) {
        return new PredictionResult(
                PredictionType.PAYMENT_CONFIRMED,
                "Your payment was received today. Would you like to check your updated available credit?",
                "User has a payment recorded today",
                0.85
        );
    }

    private PredictionResult buildDuplicate(UserContext context) {
        String reason = buildDuplicateReason(context);

        return new PredictionResult(
                PredictionType.DUPLICATE_TRANSACTION,
                "We detected similar transactions. Would you like to review them now?",
                reason,
                0.75
        );
    }

    private String buildDuplicateReason(UserContext context) {
        return context.getRecentTransactions().stream()
                .collect(Collectors.groupingBy(Transaction::getAmount, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() >= 2)
                .findFirst()
                .map(entry -> "Detected " + entry.getValue() + " similar transactions for amount " + entry.getKey())
                .orElse("Detected similar transactions within short time window");
    }
}
