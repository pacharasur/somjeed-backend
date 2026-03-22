package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class RuleBasedIntentService implements IntentService {

    @Override
    public IntentType detectIntent(String message, ChatContext context) {
        String normalizedMessage = normalize(message);

        if (containsAny(
                normalizedMessage,
                "thank you",
                "thanks",
                "thx",
                "appreciate it",
                "that helped"
        )) {
            return IntentType.CLOSING;
        }
        if (containsAny(normalizedMessage, "balance", "outstanding")) {
            return IntentType.CHECK_BALANCE;
        }
        if (containsAny(normalizedMessage, "due", "payment", "overdue")) {
            return IntentType.PAYMENT_DUE;
        }
        if (containsAny(normalizedMessage, "lost", "stolen")) {
            return IntentType.REPORT_LOST_CARD;
        }
        if (containsAny(normalizedMessage, "point", "reward")) {
            return IntentType.REWARD_POINTS;
        }
        return IntentType.GENERAL_INQUIRY;
    }

    private String normalize(String message) {
        if (message == null) {
            return "";
        }
        return message.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsAny(String message, String... keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
