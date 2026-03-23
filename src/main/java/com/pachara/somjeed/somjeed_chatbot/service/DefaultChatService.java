package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.enums.ConfirmationTypeEnum;
import com.pachara.somjeed.somjeed_chatbot.enums.IntentTypeEnum;
import com.pachara.somjeed.somjeed_chatbot.enums.PredictionTypeEnum;
import com.pachara.somjeed.somjeed_chatbot.intent.IntentHandler;
import com.pachara.somjeed.somjeed_chatbot.intent.IntentService;
import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.model.request.ChatRequest;
import com.pachara.somjeed.somjeed_chatbot.model.response.ChatResponse;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.pachara.somjeed.somjeed_chatbot.util.TextUtils.normalize;

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

        return handleInit(message, userContext, context)
                .or(() -> handleConfirmation(message, userContext, context))
                .or(() -> handleGreeting(message, userContext, context))
                .orElseGet(() -> handleIntentFallback(message, context));
    }

    private Optional<ChatResponse> handleInit(String message, UserContext userContext, ChatContext context) {
        if (!"init".equalsIgnoreCase(message)) {
            return Optional.empty();
        }

        List<String> responses = new ArrayList<>();
        responses.add(greetingService.generateGreeting());

        var prediction = predictionService.predict(userContext);
        prediction.ifPresent(p -> {
            setPredictionContext(context, p.predictionType());
            responses.add(p.message());
        });

        return Optional.of(new ChatResponse(responses));
    }

    private Optional<ChatResponse> handleConfirmation(String message, UserContext userContext, ChatContext context) {

        if (!context.isAwaitingConfirmation()) {
            return Optional.empty();
        }

        ConfirmationTypeEnum type = resolveConfirmation(message);

        return switch (type) {
            case YES -> Optional.of(handlePredictionFollowUp(context, userContext));
            case NO -> Optional.of(handleNoResponse(context, message));
            default -> Optional.empty();
        };
    }

    private ConfirmationTypeEnum resolveConfirmation(String message) {
        if (isYes(message)) return ConfirmationTypeEnum.YES;
        if (isNo(message)) return ConfirmationTypeEnum.NO;
        return ConfirmationTypeEnum.UNKNOWN;
    }

    private ChatResponse handleNoResponse(ChatContext context, String message) {

        if (context.isAwaitingCancellation()) {
            resetContext(context);
            return new ChatResponse(List.of("Okay, no transactions were cancelled."));
        }

        resetContext(context);
        return handleIntentFallback(message, context);
    }

    private Optional<ChatResponse> handleGreeting(String message, UserContext userContext, ChatContext context) {

        if (!isGreetingTrigger(message) || context.isAwaitingConfirmation()) {
            return Optional.empty();
        }

        return predictionService.predict(userContext)
                .map(prediction -> {
                    setPredictionContext(context, prediction.predictionType());
                    return new ChatResponse(List.of(prediction.message()));
                });
    }

    private void setPredictionContext(ChatContext context, PredictionTypeEnum type) {
        context.setAwaitingConfirmation(true);
        context.setAwaitingCancellation(false);
        context.setLastPredictionType(type);
    }

    private void resetContext(ChatContext context) {
        context.setAwaitingConfirmation(false);
        context.setAwaitingCancellation(false);
        context.setLastPredictionType(null);
    }

    private ChatResponse handleIntentFallback(String message, ChatContext context) {
        IntentTypeEnum intent = intentService.detectIntent(message);

        List<String> messages = Optional.ofNullable(intentHandler.handle(intent, context))
                .orElseGet(Collections::emptyList);

        return new ChatResponse(messages);
    }

    private ChatResponse handlePredictionFollowUp(ChatContext context, UserContext userContext) {
        PredictionTypeEnum type = context.getLastPredictionType();

        if (PredictionTypeEnum.DUPLICATE_TRANSACTION.equals(type)) {
            return handleDuplicate(context);
        }

        ChatResponse response = switch (type) {
            case OVERDUE -> handleOverdue(userContext);
            case PAYMENT_CONFIRMED -> handlePaymentConfirmed();
            default -> new ChatResponse(List.of("How can I assist you further?"));
        };

        resetContext(context);
        return response;
    }

    private ChatResponse handleOverdue(UserContext userContext) {
        String message = "Your current outstanding balance is " + formatAmount(userContext)
                + " THB, and your due date was "
                + userContext.getDueDate().format(DATE_FORMATTER) + ".";

        return new ChatResponse(List.of(message));
    }

    private ChatResponse handlePaymentConfirmed() {
        return new ChatResponse(List.of("Your updated available credit is 80,000 THB."));
    }

    private ChatResponse handleDuplicate(ChatContext context) {
        if (!context.isAwaitingCancellation()) {
            context.setAwaitingCancellation(true);
            return new ChatResponse(List.of("Would you like me to cancel these duplicate transactions?"));
        }

        return new ChatResponse(List.of("Duplicate transactions have been cancelled successfully."));
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
                        false,
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
