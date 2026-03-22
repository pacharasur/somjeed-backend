package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;

import java.util.List;

public interface IntentHandler {

    List<String> handle(IntentType intent, ChatContext context);
}
