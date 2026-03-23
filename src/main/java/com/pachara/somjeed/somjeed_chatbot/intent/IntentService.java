package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.enums.IntentTypeEnum;
import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;

public interface IntentService {

    IntentTypeEnum detectIntent(String message, ChatContext context);
}
