package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;

public interface IntentService {

    IntentType detectIntent(String message, ChatContext context);
}
