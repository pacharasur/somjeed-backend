package com.pachara.somjeed.somjeed_chatbot.prediction;

import com.pachara.somjeed.somjeed_chatbot.enums.PredictionTypeEnum;

public record PredictionResult(
        PredictionTypeEnum predictionType,
        String message,
        String reason,
        double confidence
) {
}
