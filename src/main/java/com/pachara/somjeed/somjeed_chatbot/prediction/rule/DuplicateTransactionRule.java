package com.pachara.somjeed.somjeed_chatbot.prediction.rule;

import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Component
public class DuplicateTransactionRule implements PredictionRule {

    private static final long WINDOW_MINUTES = 10;


    @Override
    public boolean matches(UserContext context) {
        List<Transaction> transactions = context.getRecentTransactions();
        if (transactions == null || transactions.size() < 2) {
            return false;
        }

        List<Transaction> sorted = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .toList();

        for (int i = 0; i < sorted.size() - 1; i++) {
            if (isDuplicate(sorted.get(i), sorted.get(i + 1))) {
                return true;
            }
        }

        return false;
    }

    private boolean isDuplicate(Transaction current, Transaction next) {
        boolean sameAmount = current.getAmount().compareTo(next.getAmount()) == 0;

        long minutes = Math.abs(
                Duration.between(current.getTimestamp(), next.getTimestamp()).toMinutes()
        );

        return sameAmount && minutes <= WINDOW_MINUTES;
    }

    @Override
    public int score() {
        return 70;
    }

    @Override
    public PredictionType type() {
        return PredictionType.DUPLICATE_TRANSACTION;
    }
}
