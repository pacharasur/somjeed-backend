package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.model.request.ChatRequest;
import com.pachara.somjeed.somjeed_chatbot.model.response.ChatResponse;

public interface ChatService {

    ChatResponse handle(ChatRequest request);
}
