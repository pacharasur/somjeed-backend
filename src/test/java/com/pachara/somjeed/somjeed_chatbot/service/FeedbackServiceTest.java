package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.model.entity.Feedback;
import com.pachara.somjeed.somjeed_chatbot.model.request.FeedbackRequest;
import com.pachara.somjeed.somjeed_chatbot.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    void submitFeedback_shouldSaveFeedback_whenRequestValid() {
        FeedbackRequest request = new FeedbackRequest(" user_001 ", 5);

        feedbackService.submitFeedback(request);

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(captor.capture());
        assertEquals("user_001", captor.getValue().getUserId());
        assertEquals(5, captor.getValue().getRating());
    }
}
