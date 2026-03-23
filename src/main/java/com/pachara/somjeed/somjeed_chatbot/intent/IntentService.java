package com.pachara.somjeed.somjeed_chatbot.intent;

import com.pachara.somjeed.somjeed_chatbot.enums.IntentTypeEnum;

public interface IntentService {

    IntentTypeEnum detectIntent(String message);
}
