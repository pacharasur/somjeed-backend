package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.enums.IntentTypeEnum;
import com.pachara.somjeed.somjeed_chatbot.model.domain.ChatContext;

import java.util.List;

public interface IntentHandler {

    List<String> handle(IntentTypeEnum intent, ChatContext context);
}
