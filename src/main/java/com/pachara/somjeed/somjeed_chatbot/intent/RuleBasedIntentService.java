package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import static com.pachara.somjeed.somjeed_chatbot.util.TextUtils.normalize;

@Service
public class RuleBasedIntentService implements IntentService {

    private static final Map<IntentType, List<String>> INTENT_KEYWORDS = Map.of(
            IntentType.CLOSING, List.of("thank you", "thanks", "thx", "appreciate it", "that helped"),
            IntentType.CHECK_BALANCE, List.of("balance", "outstanding"),
            IntentType.PAYMENT_DUE, List.of("due", "payment", "overdue"),
            IntentType.REPORT_LOST_CARD, List.of("lost", "stolen"),
            IntentType.REWARD_POINTS, List.of("point", "reward")
    );

    @Override
    public IntentType detectIntent(String message, ChatContext context) {
        String normalized = normalize(message);

        return INTENT_KEYWORDS.entrySet().stream()
                .filter(entry -> containsAny(normalized, entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(IntentType.GENERAL_INQUIRY);
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
