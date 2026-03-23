package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.enums.IntentTypeEnum;
import com.pachara.somjeed.somjeed_chatbot.enums.PredictionTypeEnum;
import com.pachara.somjeed.somjeed_chatbot.intent.IntentHandler;
import com.pachara.somjeed.somjeed_chatbot.intent.IntentService;
import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;
import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import com.pachara.somjeed.somjeed_chatbot.model.request.ChatRequest;
import com.pachara.somjeed.somjeed_chatbot.model.response.ChatResponse;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionResult;
import com.pachara.somjeed.somjeed_chatbot.prediction.PredictionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        when(userContextService.getUserContext("user_001")).thenReturn(userContext);
        when(greetingService.generateGreeting()).thenReturn("Good morning, on a sunshine day!");
        when(predictionService.predict(userContext)).thenReturn(Optional.of(
                new PredictionResult(PredictionTypeEnum.OVERDUE, "Prediction question")
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
        verify(intentService, never()).detectIntent(any());
        verify(predictionService, times(1)).predict(userContext);
    }

    @Test
    void handle_initWithoutPrediction_thenYes_shouldFallBackToIntent() {
        UserContext userContext = userContext("user_001", LocalDate.now().plusDays(2), new BigDecimal("1000"), false, List.of());
        when(userContextService.getUserContext("user_001")).thenReturn(userContext, userContext);
        when(greetingService.generateGreeting()).thenReturn("Good evening, stay dry out there!");
        when(predictionService.predict(userContext)).thenReturn(Optional.empty());
        when(intentService.detectIntent(eq("yes"))).thenReturn(IntentTypeEnum.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentTypeEnum.GENERAL_INQUIRY), any(ChatContext.class)))
                .thenReturn(List.of("How can I assist you further?"));

        ChatResponse initResponse = chatService.handle(request("user_001", "init"));
        assertEquals(List.of("Good evening, stay dry out there!"), initResponse.getMessages());

        ChatResponse yesResponse = chatService.handle(request("user_001", "yes"));
        assertEquals(List.of("How can I assist you further?"), yesResponse.getMessages());
        verify(intentService, times(1)).detectIntent(eq("yes"));
    }

    @Test
    void handle_greetingWithPrediction_shouldReturnOnlyPredictionMessage() {
        UserContext userContext = userContext("user_002", LocalDate.now().plusDays(4), new BigDecimal("8000"), false, List.of());
        when(userContextService.getUserContext("user_002")).thenReturn(userContext);
        when(predictionService.predict(userContext)).thenReturn(Optional.of(
                new PredictionResult(PredictionTypeEnum.PAYMENT_CONFIRMED, "Prediction from greeting")
        ));

        ChatResponse response = chatService.handle(request("user_002", "Hello"));

        assertEquals(List.of("Prediction from greeting"), response.getMessages());
        verify(greetingService, never()).generateGreeting();
        verify(intentService, never()).detectIntent(any());
    }

    @Test
    void handle_greetingWithoutPrediction_shouldFallBackToIntent() {
        UserContext userContext = userContext("user_003", LocalDate.now().plusDays(2), new BigDecimal("500"), false, List.of());
        when(userContextService.getUserContext("user_003")).thenReturn(userContext);
        when(predictionService.predict(userContext)).thenReturn(Optional.empty());
        when(intentService.detectIntent(eq("hi"))).thenReturn(IntentTypeEnum.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentTypeEnum.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(List.of("Fallback"));

        ChatResponse response = chatService.handle(request("user_003", "hi"));

        assertEquals(List.of("Fallback"), response.getMessages());
        verify(intentService).detectIntent(eq("hi"));
    }

    @Test
    void handle_whenAwaitingConfirmationAndMessageUnknown_shouldFallThroughToIntent() {
        UserContext userContext = userContext("user_004", LocalDate.now().minusDays(1), new BigDecimal("120000"), false, List.of());
        when(userContextService.getUserContext("user_004")).thenReturn(userContext, userContext);
        when(predictionService.predict(userContext)).thenReturn(Optional.of(
                new PredictionResult(PredictionTypeEnum.OVERDUE, "Overdue question")
        ));
        when(intentService.detectIntent(eq("maybe"))).thenReturn(IntentTypeEnum.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentTypeEnum.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(List.of("Fallback from intent"));

        chatService.handle(request("user_004", "hello"));
        ChatResponse response = chatService.handle(request("user_004", "maybe"));

        assertEquals(List.of("Fallback from intent"), response.getMessages());
        verify(predictionService, times(1)).predict(userContext);
        verify(intentService, times(1)).detectIntent(eq("maybe"));
    }

    @Test
    void handle_shouldReturnPaymentConfirmedDetail_whenUserRepliesYes() {
        UserContext userContext = userContext("user_005", LocalDate.now().plusDays(3), new BigDecimal("10000"), true, List.of());

        when(userContextService.getUserContext("user_005")).thenReturn(userContext, userContext);

        when(predictionService.predict(userContext)).thenReturn(Optional.of(new PredictionResult(
                PredictionTypeEnum.PAYMENT_CONFIRMED,
                "Payment confirmed question"
        )));

        chatService.handle(request("user_005", "hey"));

        ChatResponse followUp = chatService.handle(request("user_005", "yes"));

        assertEquals(List.of("Your updated available credit is 80,000 THB."), followUp.getMessages());
    }

    @Test
    void handle_shouldFallbackToIntent_afterFollowUpIsCompleted() {
        UserContext userContext = userContext("user_005", LocalDate.now().plusDays(3), new BigDecimal("10000"), true, List.of());

        when(userContextService.getUserContext("user_005")).thenReturn(userContext, userContext, userContext);

        when(predictionService.predict(userContext)).thenReturn(Optional.of(new PredictionResult(
                PredictionTypeEnum.PAYMENT_CONFIRMED,
                "Payment confirmed question"
        )));

        when(intentService.detectIntent(eq("yes"))).thenReturn(IntentTypeEnum.GENERAL_INQUIRY);

        when(intentHandler.handle(eq(IntentTypeEnum.GENERAL_INQUIRY), any())).thenReturn(List.of("Intent fallback"));

        chatService.handle(request("user_005", "hey"));
        chatService.handle(request("user_005", "yes"));

        ChatResponse afterReset = chatService.handle(request("user_005", "yes"));

        assertEquals(List.of("Intent fallback"), afterReset.getMessages());
        verify(intentService, times(1)).detectIntent(eq("yes"));
    }

    @Test
    void handle_duplicatePrediction_yesThenYes_shouldAskCancelThenConfirmCancelled() {
        UserContext userContext = userContext("user_006", LocalDate.now().plusDays(5), new BigDecimal("3000"), false, List.of());
        when(userContextService.getUserContext("user_006")).thenReturn(userContext, userContext, userContext);
        when(predictionService.predict(userContext)).thenReturn(Optional.of(
                new PredictionResult(PredictionTypeEnum.DUPLICATE_TRANSACTION, "Detected duplicates question")
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
                new PredictionResult(PredictionTypeEnum.DUPLICATE_TRANSACTION, "Detected duplicates question")
        ));
        when(intentService.detectIntent(eq("no"))).thenReturn(IntentTypeEnum.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentTypeEnum.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(List.of("Intent fallback after reset"));

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
        when(intentService.detectIntent(eq("anything"))).thenReturn(IntentTypeEnum.GENERAL_INQUIRY);
        when(intentHandler.handle(eq(IntentTypeEnum.GENERAL_INQUIRY), any(ChatContext.class))).thenReturn(null);

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
        when(intentService.detectIntent(any())).thenReturn(IntentTypeEnum.GENERAL_INQUIRY);
        ArgumentCaptor<ChatContext> captor = ArgumentCaptor.forClass(ChatContext.class);
        when(intentHandler.handle(eq(IntentTypeEnum.GENERAL_INQUIRY), captor.capture())).thenReturn(List.of("ok"));
        chatService.handle(request("user_009", "first"));
        chatService.handle(request("user_009", "second"));
        verify(intentService, times(2)).detectIntent(any());
        ChatContext latest = captor.getAllValues().get(1);
        assertEquals(LocalDate.of(2026, 4, 1), latest.getDueDate());
        assertEquals(new BigDecimal("2500"), latest.getOutstandingBalance());
        assertEquals(1, latest.getTransactions().size());
        assertFalse(latest.isAwaitingConfirmation());
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
