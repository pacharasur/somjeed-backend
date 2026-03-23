package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.enums.IntentTypeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleBasedIntentServiceTest {

    private final RuleBasedIntentService service = new RuleBasedIntentService();

    @Test
    void detectIntent_shouldDetectClosing() {
        assertEquals(IntentTypeEnum.CLOSING, service.detectIntent("Thanks, that helped"));
    }

    @Test
    void detectIntent_shouldDetectCheckBalance() {
        assertEquals(IntentTypeEnum.CHECK_BALANCE, service.detectIntent("Please check my outstanding"));
    }

    @Test
    void detectIntent_shouldDetectPaymentDue() {
        assertEquals(IntentTypeEnum.PAYMENT_DUE, service.detectIntent("When is my payment due?"));
    }

    @Test
    void detectIntent_shouldDetectReportLostCard() {
        assertEquals(IntentTypeEnum.REPORT_LOST_CARD, service.detectIntent("My card was stolen"));
    }

    @Test
    void detectIntent_shouldDetectRewardPoints() {
        assertEquals(IntentTypeEnum.REWARD_POINTS, service.detectIntent("How many reward points do I have?"));
    }

    @Test
    void detectIntent_shouldReturnGeneralInquiry_whenNoKeyword() {
        assertEquals(IntentTypeEnum.GENERAL_INQUIRY, service.detectIntent("I need some help"));
    }

    @Test
    void detectIntent_shouldHandleNullMessage_asGeneralInquiry() {
        assertEquals(IntentTypeEnum.GENERAL_INQUIRY, service.detectIntent(null));
    }

    @Test
    void detectIntent_shouldHandleEmptyMessage_asGeneralInquiry() {
        assertEquals(IntentTypeEnum.GENERAL_INQUIRY, service.detectIntent(""));
    }

    @Test
    void detectIntent_shouldHandleBlankMessage_asGeneralInquiry() {
        assertEquals(IntentTypeEnum.GENERAL_INQUIRY, service.detectIntent("   "));
    }

    @Test
    void detectIntent_shouldIgnoreCaseAndSpaces() {
        assertEquals(IntentTypeEnum.CLOSING, service.detectIntent("   THANK YOU   "));
    }

    @Test
    void detectIntent_shouldHandleNullContext_whenDetecting() {
        assertEquals(IntentTypeEnum.CHECK_BALANCE, service.detectIntent("balance"));
    }
}
