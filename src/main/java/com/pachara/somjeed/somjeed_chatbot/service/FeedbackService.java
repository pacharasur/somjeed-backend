package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.model.entity.Feedback;
import com.pachara.somjeed.somjeed_chatbot.model.request.FeedbackRequest;
import com.pachara.somjeed.somjeed_chatbot.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public void submitFeedback(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setUserId(request.getUserId().trim());
        feedback.setRating(request.getRating());
        feedbackRepository.save(feedback);
    }
}
