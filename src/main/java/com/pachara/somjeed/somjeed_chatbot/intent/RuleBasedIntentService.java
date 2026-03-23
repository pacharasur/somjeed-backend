package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.enums.IntentTypeEnum;
import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import static com.pachara.somjeed.somjeed_chatbot.util.TextUtils.normalize;

@Service
public class RuleBasedIntentService implements IntentService {

    private static final Map<IntentTypeEnum, List<String>> INTENT_KEYWORDS = Map.of(
            IntentTypeEnum.CLOSING, List.of("thank you", "thanks", "thx", "appreciate it", "that helped"),
            IntentTypeEnum.CHECK_BALANCE, List.of("balance", "outstanding"),
            IntentTypeEnum.PAYMENT_DUE, List.of("due", "payment", "overdue"),
            IntentTypeEnum.REPORT_LOST_CARD, List.of("lost", "stolen"),
            IntentTypeEnum.REWARD_POINTS, List.of("point", "reward")
    );

    @Override
    public IntentTypeEnum detectIntent(String message, ChatContext context) {
        String normalized = normalize(message);

        return INTENT_KEYWORDS.entrySet().stream()
                .filter(entry -> containsAny(normalized, entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(IntentTypeEnum.GENERAL_INQUIRY);
    }

    private boolean containsAny(String message, List<String> keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
