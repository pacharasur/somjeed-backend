package com.pachara.somjeed.somjeed_chatbot.prediction.rule;

import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class OverdueRule implements PredictionRule {

    private final Clock clock;

    @Override
    public boolean matches(UserContext context) {
        return context.getDueDate() != null && LocalDate.now(clock).isAfter(context.getDueDate());
    }

    @Override
    public int score() {
        return 100;
    }

    @Override
    public PredictionType type() {
        return PredictionType.OVERDUE;
    }
}
