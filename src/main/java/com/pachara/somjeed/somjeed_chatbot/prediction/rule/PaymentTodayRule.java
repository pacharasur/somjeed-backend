package com.pachara.somjeed.somjeed_chatbot.prediction.rule;

import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.enums.PredictionTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class PaymentTodayRule implements PredictionRule {

    @Override
    public boolean matches(UserContext context) {
        return context.isHasPaymentToday();
    }

    @Override
    public int score() {
        return 80;
    }

    @Override
    public PredictionTypeEnum type() {
        return PredictionTypeEnum.PAYMENT_CONFIRMED;
    }
}
