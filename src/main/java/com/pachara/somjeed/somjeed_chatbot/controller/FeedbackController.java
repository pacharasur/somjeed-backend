package com.pachara.somjeed.somjeed_chatbot.controller;

import com.pachara.somjeed.somjeed_chatbot.model.response.ChatResponse;
import jakarta.validation.Valid;
import com.pachara.somjeed.somjeed_chatbot.model.request.FeedbackRequest;
import com.pachara.somjeed.somjeed_chatbot.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ChatResponse> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        feedbackService.submitFeedback(request);
        return ResponseEntity.ok(new ChatResponse(List.of("Feedback submitted successfully.")));
    }
}
