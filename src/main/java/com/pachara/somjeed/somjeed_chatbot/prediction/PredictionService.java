package com.pachara.somjeed.somjeed_chatbot.prediction;

import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;

import java.util.Optional;

public interface PredictionService {

    Optional<PredictionResult> predict(UserContext context);
}
