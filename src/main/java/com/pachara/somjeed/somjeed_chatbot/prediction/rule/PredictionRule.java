package com.pachara.somjeed.somjeed_chatbot.prediction.rule;

import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionType;

public interface PredictionRule {

    boolean matches(UserContext context);

    int score();

    PredictionType type();
}
