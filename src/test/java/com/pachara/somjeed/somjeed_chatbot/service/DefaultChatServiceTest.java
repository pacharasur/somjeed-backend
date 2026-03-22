package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.intent.IntentHandler;
import com.pachara.somjeed.somjeed_chatbot.intent.IntentService;
import com.pachara.somjeed.somjeed_chatbot.intent.IntentType;
import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;
import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.model.request.ChatRequest;
import com.pachara.somjeed.somjeed_chatbot.model.response.ChatResponse;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionResult;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionService;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultChatServiceTest {

    @Mock
    private GreetingService greetingService;
    @Mock
    private PredictionService predictionService;
    @Mock
    private UserContextService userContextService;
    @Mock
    private IntentService intentService;
    @Mock
    private IntentHandler intentHandler;

    @InjectMocks
    private DefaultChatService chatService;

    @Test
    void handle_initWithPrediction_thenYes_shouldReturnGreetingPredictionAndOverdueDetails() {
        UserContext userContext = userContext(
                "user_001",
                LocalDate.of(2025, 9, 1),
                new BigDecimal("120000"),
                false,
                List.of()
        );
        when(userContextService.getUserContext("user_001")).thenReturn(userContext, userContext);
        when(greetingService.generateGreeting()).thenReturn("Good morning, on a sunshine day!");
        when(predictionService.predict(userContext)).thenReturn(Optional.of(
                new PredictionResult(PredictionType.OVERDUE, "Prediction question", "reason", 0.95)
        ));

        ChatResponse initResponse = chatService.handle(request("user_001", " INIT "));
        assertEquals(
                List.of("Good morning, on a sunshine day!", "Prediction question"),
                initResponse.getMessages()
        );

        ChatResponse yesResponse = chatService.handle(request("user_001", "yes"));
        assertEquals(
                List.of("Your current outstanding balance is 120,000 THB, and your due date was 1 September 2025."),
                yesResponse.getMessages()
        );
        verify(intentService, never()).detectIntent(any(), any());
        verify(predictionService, times(1)).predict(userContext);
    }

    @Test
    void handle_initWithoutPrediction_thenYes_shouldFallBackToIntent() {
        UserContext userContext = userContext("user_001", LocalDate.now().plusDays(2), new BigDecimal("1000"), false, List.of());
        when(userContextService.getUserContext("user_001")).thenReturn(userContext, userContext);
        when(greetingService.generateGreeting()).thenReturn("Good evening, stay dry out there!");
        when(predictionService.predict(userContext)).thenReturn(Optional.empty());
        when(intentService.detectIntent(eq("yes"), any(ChatContext.class))).thenReturn(IntentType.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentType.GENERAL_INQUIRY), any(ChatContext.class)))
                .thenReturn(List.of("How can I assist you further?"));

        ChatResponse initResponse = chatService.handle(request("user_001", "init"));
        assertEquals(List.of("Good evening, stay dry out there!"), initResponse.getMessages());

        ChatResponse yesResponse = chatService.handle(request("user_001", "yes"));
        assertEquals(List.of("How can I assist you further?"), yesResponse.getMessages());
        verify(intentService, times(1)).detectIntent(eq("yes"), any(ChatContext.class));
    }

    @Test
    void handle_greetingWithPrediction_shouldReturnOnlyPredictionMessage() {
        UserContext userContext = userContext("user_002", LocalDate.now().plusDays(4), new BigDecimal("8000"), false, List.of());
        when(userContextService.getUserContext("user_002")).thenReturn(userContext);
        when(predictionService.predict(userContext)).thenReturn(Optional.of(
                new PredictionResult(PredictionType.PAYMENT_CONFIRMED, "Prediction from greeting", "reason", 0.8)
        ));

        ChatResponse response = chatService.handle(request("user_002", "Hello"));

        assertEquals(List.of("Prediction from greeting"), response.getMessages());
        verify(greetingService, never()).generateGreeting();
        verify(intentService, never()).detectIntent(any(), any());
    }

    @Test
    void handle_greetingWithoutPrediction_shouldFallBackToIntent() {
        UserContext userContext = userContext("user_003", LocalDate.now().plusDays(2), new BigDecimal("500"), false, List.of());
        when(userContextService.getUserContext("user_003")).thenReturn(userContext);
        when(predictionService.predict(userContext)).thenReturn(Optional.empty());
        when(intentService.detectIntent(eq("hi"), any(ChatContext.class))).thenReturn(IntentType.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentType.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(List.of("Fallback"));

        ChatResponse response = chatService.handle(request("user_003", "hi"));

        assertEquals(List.of("Fallback"), response.getMessages());
        verify(intentService).detectIntent(eq("hi"), any(ChatContext.class));
    }

    @Test
    void handle_whenAwaitingConfirmationAndMessageUnknown_shouldFallThroughToIntent() {
        UserContext userContext = userContext("user_004", LocalDate.now().minusDays(1), new BigDecimal("120000"), false, List.of());
        when(userContextService.getUserContext("user_004")).thenReturn(userContext, userContext);
        when(predictionService.predict(userContext)).thenReturn(Optional.of(
                new PredictionResult(PredictionType.OVERDUE, "Overdue question", "reason", 0.95)
        ));
        when(intentService.detectIntent(eq("maybe"), any(ChatContext.class))).thenReturn(IntentType.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentType.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(List.of("Fallback from intent"));

        chatService.handle(request("user_004", "hello"));
        ChatResponse response = chatService.handle(request("user_004", "maybe"));

        assertEquals(List.of("Fallback from intent"), response.getMessages());
        verify(predictionService, times(1)).predict(userContext);
        verify(intentService, times(1)).detectIntent(eq("maybe"), any(ChatContext.class));
    }

    @Test
    void handle_paymentConfirmedYesFollowUp_shouldReturnDetailThenResetContext() {
        UserContext userContext = userContext("user_005", LocalDate.now().plusDays(3), new BigDecimal("10000"), true, List.of());
        when(userContextService.getUserContext("user_005")).thenReturn(userContext, userContext, userContext);
        when(predictionService.predict(userContext)).thenReturn(Optional.of(
                new PredictionResult(PredictionType.PAYMENT_CONFIRMED, "Payment confirmed question", "reason", 0.85)
        ));
        when(intentService.detectIntent(eq("yes"), any(ChatContext.class))).thenReturn(IntentType.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentType.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(List.of("Intent fallback"));

        chatService.handle(request("user_005", "hey"));
        ChatResponse followUp = chatService.handle(request("user_005", "yes"));
        assertEquals(List.of("Your updated available credit is 80,000 THB."), followUp.getMessages());

        ChatResponse afterReset = chatService.handle(request("user_005", "yes"));
        assertEquals(List.of("Intent fallback"), afterReset.getMessages());
        verify(intentService, times(1)).detectIntent(eq("yes"), any(ChatContext.class));
    }

    @Test
    void handle_duplicatePrediction_yesThenYes_shouldAskCancelThenConfirmCancelled() {
        UserContext userContext = userContext("user_006", LocalDate.now().plusDays(5), new BigDecimal("3000"), false, List.of());
        when(userContextService.getUserContext("user_006")).thenReturn(userContext, userContext, userContext);
        when(predictionService.predict(userContext)).thenReturn(Optional.of(
                new PredictionResult(PredictionType.DUPLICATE_TRANSACTION, "Detected duplicates question", "reason", 0.7)
        ));

        chatService.handle(request("user_006", "hi"));
        ChatResponse firstYes = chatService.handle(request("user_006", "yes"));
        assertEquals(List.of("Would you like me to cancel these duplicate transactions?"), firstYes.getMessages());

        ChatResponse secondYes = chatService.handle(request("user_006", "yes"));
        assertEquals(List.of("Duplicate transactions have been cancelled successfully."), secondYes.getMessages());
    }

    @Test
    void handle_duplicatePrediction_yesThenNo_shouldReturnNoCancellationAndResetContext() {
        UserContext userContext = userContext("user_007", LocalDate.now().plusDays(5), new BigDecimal("3000"), false, List.of());
        when(userContextService.getUserContext("user_007")).thenReturn(userContext, userContext, userContext, userContext);
        when(predictionService.predict(userContext)).thenReturn(Optional.of(
                new PredictionResult(PredictionType.DUPLICATE_TRANSACTION, "Detected duplicates question", "reason", 0.7)
        ));
        when(intentService.detectIntent(eq("no"), any(ChatContext.class))).thenReturn(IntentType.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentType.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(List.of("Intent fallback after reset"));

        chatService.handle(request("user_007", "hi"));
        chatService.handle(request("user_007", "yes"));
        ChatResponse noCancellation = chatService.handle(request("user_007", "no"));
        assertEquals(List.of("Okay, no transactions were cancelled."), noCancellation.getMessages());

        ChatResponse afterReset = chatService.handle(request("user_007", "no"));
        assertEquals(List.of("Intent fallback after reset"), afterReset.getMessages());
    }

    @Test
    void handle_intentHandlerReturnsNull_shouldReturnEmptyList() {
        UserContext userContext = userContext("user_008", LocalDate.now().plusDays(1), new BigDecimal("1500"), false, List.of());
        when(userContextService.getUserContext("user_008")).thenReturn(userContext);
        when(intentService.detectIntent(eq("anything"), any(ChatContext.class))).thenReturn(IntentType.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentType.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(null);

        ChatResponse response = chatService.handle(request("user_008", "anything"));

        assertTrue(response.getMessages().isEmpty());
    }

    @Test
    void handle_existingContext_shouldBeUpdatedWithLatestUserContext() {
        UserContext firstContext = userContext(
                "user_009",
                LocalDate.of(2026, 3, 20),
                new BigDecimal("1000"),
                false,
                List.of(new Transaction(new BigDecimal("100"), LocalDateTime.of(2026, 3, 20, 10, 0)))
        );
        UserContext secondContext = userContext(
                "user_009",
                LocalDate.of(2026, 4, 1),
                new BigDecimal("2500"),
                false,
                List.of(new Transaction(new BigDecimal("200"), LocalDateTime.of(2026, 3, 22, 11, 30)))
        );
        when(userContextService.getUserContext("user_009")).thenReturn(firstContext, secondContext);
        when(intentService.detectIntent(any(), any(ChatContext.class))).thenReturn(IntentType.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentType.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(List.of("ok"));

        chatService.handle(request("user_009", "first"));
        chatService.handle(request("user_009", "second"));

        ArgumentCaptor<ChatContext> captor = ArgumentCaptor.forClass(ChatContext.class);
        verify(intentService, times(2)).detectIntent(any(), captor.capture());
        ChatContext latest = captor.getAllValues().get(1);
        assertEquals(LocalDate.of(2026, 4, 1), latest.getDueDate());
        assertEquals(new BigDecimal("2500"), latest.getOutstandingBalance());
        assertEquals(1, latest.getTransactions().size());
        assertFalse(latest.isAwaitingConfirmation());
    }

    @Test
    void handle_corruptedContextWithNullPredictionTypeAndYes_shouldThrowException() {
        UserContext userContext = userContext("user_010", LocalDate.now(), new BigDecimal("999"), false, List.of());
        when(userContextService.getUserContext("user_010")).thenReturn(userContext, userContext);
        when(intentService.detectIntent(eq("hello"), any(ChatContext.class))).thenReturn(IntentType.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentType.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(List.of("seed context"));

        chatService.handle(request("user_010", "hello"));

        @SuppressWarnings("unchecked")
        Map<String, ChatContext> contextStore = (Map<String, ChatContext>) ReflectionTestUtils.getField(chatService, "contextStore");
        ChatContext context = contextStore.get("user_010");
        context.setAwaitingConfirmation(true);
        context.setLastPredictionType(null);

        assertThrows(NullPointerException.class, () -> chatService.handle(request("user_010", "yes")));
    }

    private ChatRequest request(String userId, String message) {
        return new ChatRequest(userId, message);
    }

    private UserContext userContext(
            String userId,
            LocalDate dueDate,
            BigDecimal outstandingBalance,
            boolean hasPaymentToday,
            List<Transaction> transactions
    ) {
        return new UserContext(userId, dueDate, outstandingBalance, hasPaymentToday, transactions);
    }
}
