package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.intent.IntentHandler;
import com.pachara.somjeed.somjeed_chatbot.intent.IntentService;
import com.pachara.somjeed.somjeed_chatbot.intent.IntentType;
import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.model.request.ChatRequest;
import com.pachara.somjeed.somjeed_chatbot.model.response.ChatResponse;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionService;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionType;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DefaultChatService implements ChatService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH);
    private final Map<String, ChatContext> contextStore = new ConcurrentHashMap<>();

    private final GreetingService greetingService;
    private final PredictionService predictionService;
    private final UserContextService userContextService;
    private final IntentService intentService;
    private final IntentHandler intentHandler;

    @Override
    public ChatResponse handle(ChatRequest request) {
        String message = normalize(request.getMessage());
        UserContext userContext = userContextService.getUserContext(request.getUserId());
        ChatContext context = getOrCreateContext(userContext);

        if ("init".equalsIgnoreCase(message)) {

            List<String> responses = new ArrayList<>();

            responses.add(greetingService.generateGreeting());

            var prediction = predictionService.predict(userContext);
            if (prediction.isPresent()) {
                context.setAwaitingConfirmation(true);
                context.setLastPredictionType(prediction.get().predictionType());

                responses.add(prediction.get().message());
            }

            return new ChatResponse(responses);
        }

        if (context.isAwaitingConfirmation()) {
            if (isYes(message)) {
                return handlePredictionFollowUp(context, userContext);
            }
            if (isNo(message)) {
                context.setAwaitingConfirmation(false);
                context.setLastPredictionType(null);
                return handleIntentFallback(message, context);
            }
        }

        if (isGreetingTrigger(message) && !context.isAwaitingConfirmation()) {
            var prediction = predictionService.predict(userContext);
            if (prediction.isPresent()) {
                context.setAwaitingConfirmation(true);
                context.setLastPredictionType(prediction.get().predictionType());
                return new ChatResponse(List.of(prediction.get().message()));
            }
        }

        return handleIntentFallback(message, context);
    }

    private ChatResponse handleIntentFallback(String message, ChatContext context) {
        List<String> messages = new ArrayList<>();
        IntentType intent = intentService.detectIntent(message, context);
        messages.addAll(intentHandler.handle(intent, context));
        return new ChatResponse(messages);
    }

    private ChatResponse handlePredictionFollowUp(ChatContext context, UserContext userContext) {
        String message;
        PredictionType predictionType = context.getLastPredictionType();

        if (PredictionType.OVERDUE.equals(predictionType)) {
            message = "Your current outstanding balance is " + formatAmount(userContext)
                    + " THB, and your due date was " + userContext.getDueDate().format(DATE_FORMATTER) + ".";
        } else if (PredictionType.PAYMENT_CONFIRMED.equals(predictionType)) {
            message = "Your updated available credit is 80,000 THB.";
        } else if (PredictionType.DUPLICATE_TRANSACTION.equals(predictionType)) {
            message = "Please review these transactions...";
        } else {
            message = "How can I assist you further?";
        }

        context.setAwaitingConfirmation(false);
        context.setLastPredictionType(null);
        return new ChatResponse(List.of(message));
    }

    private ChatContext getOrCreateContext(UserContext userContext) {
        return contextStore.compute(userContext.getUserId(), (userId, existing) -> {
            if (existing == null) {
                return new ChatContext(
                        userContext.getDueDate(),
                        userContext.getOutstandingBalance(),
                        0,
                        userContext.isHasPaymentToday() ? java.time.LocalDate.now() : null,
                        userContext.getRecentTransactions(),
                        null,
                        false
                );
            }

            existing.setDueDate(userContext.getDueDate());
            existing.setOutstandingBalance(userContext.getOutstandingBalance());
            existing.setLastPaymentDate(userContext.isHasPaymentToday() ? java.time.LocalDate.now() : null);
            existing.setTransactions(userContext.getRecentTransactions());
            return existing;
        });
    }

    private String normalize(String message) {
        return message.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isYes(String message) {
        return "yes".equals(message)
                || "y".equals(message)
                || "ok".equals(message)
                || "sure".equals(message);
    }

    private boolean isNo(String message) {
        return "no".equals(message)
                || "n".equals(message)
                || "nope".equals(message);
    }

    private boolean isGreetingTrigger(String message) {
        return "hi".equals(message)
                || "hello".equals(message)
                || "hey".equals(message);
    }

    private String formatAmount(UserContext userContext) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(userContext.getOutstandingBalance());
    }
}
