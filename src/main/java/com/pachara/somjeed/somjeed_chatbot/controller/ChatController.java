package com.pachara.somjeed.somjeed_chatbot.controller;

import jakarta.validation.Valid;
import com.pachara.somjeed.somjeed_chatbot.model.request.ChatRequest;
import com.pachara.somjeed.somjeed_chatbot.model.response.ChatResponse;
import com.pachara.somjeed.somjeed_chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest chatRequest) {
        return ResponseEntity.ok(chatService.handle(chatRequest));
    }
}
