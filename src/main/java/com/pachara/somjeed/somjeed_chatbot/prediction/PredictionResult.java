package com.pachara.somjeed.somjeed_chatbot.prediction;

public record PredictionResult(
        PredictionType predictionType,
        String message,
        String reason,
        double confidence
) {
}
