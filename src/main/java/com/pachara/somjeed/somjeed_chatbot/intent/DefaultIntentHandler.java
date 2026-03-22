package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@Service
public class DefaultIntentHandler implements IntentHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH);
    private final Map<IntentType, Function<ChatContext, List<String>>> handlers = buildHandlers();

    @Override
    public List<String> handle(IntentType intent, ChatContext context) {
        return handlers.getOrDefault(intent, handlers.get(IntentType.GENERAL_INQUIRY)).apply(context);
    }

    private Map<IntentType, Function<ChatContext, List<String>>> buildHandlers() {
        Map<IntentType, Function<ChatContext, List<String>>> mapping = new EnumMap<>(IntentType.class);
        mapping.put(IntentType.CHECK_BALANCE, this::handleCheckBalance);
        mapping.put(IntentType.PAYMENT_DUE, this::handlePaymentDue);
        mapping.put(IntentType.REPORT_LOST_CARD, this::handleReportLostCard);
        mapping.put(IntentType.REWARD_POINTS, this::handleRewardPoints);
        mapping.put(IntentType.CLOSING, this::handleClosing);
        mapping.put(IntentType.GENERAL_INQUIRY, this::handleGeneralInquiry);
        return mapping;
    }

    private List<String> handleCheckBalance(ChatContext context) {
        return List.of("Your current outstanding balance is " + formatAmount(context) + " THB.");
    }

    private List<String> handlePaymentDue(ChatContext context) {
        return List.of("Your payment is due on " + context.getDueDate().format(DATE_FORMATTER) + ".");
    }

    private List<String> handleReportLostCard(ChatContext context) {
        return List.of("Please contact our support immediately to block your card.");
    }

    private List<String> handleRewardPoints(ChatContext context) {
        return List.of("You have " + formatPoints(context) + " reward points available.");
    }

    private List<String> handleClosing(ChatContext context) {
        return List.of("You're welcome. Glad I could help. Have a great day.");
    }

    private List<String> handleGeneralInquiry(ChatContext context) {
        return List.of("How can I assist you further?");
    }

    private String formatAmount(ChatContext context) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(context.getOutstandingBalance());
    }

    private String formatPoints(ChatContext context) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(context.getRewardPoints());
    }
}
