package com.pachara.somjeed.somjeed_chatbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachara.somjeed.somjeed_chatbot.model.request.FeedbackRequest;
import com.pachara.somjeed.somjeed_chatbot.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedbackController.class)
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FeedbackService feedbackService;

    @Test
    void submitFeedback_shouldReturnOk_whenRequestValid() throws Exception {
        FeedbackRequest request = new FeedbackRequest("user_001", 5);

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0]").value("Feedback submitted successfully."));
    }

    @Test
    void submitFeedback_shouldReturnBadRequest_whenRequestInvalid() throws Exception {
        doThrow(new IllegalArgumentException("Invalid feedback request"))
                .when(feedbackService).submitFeedback(any(FeedbackRequest.class));

        FeedbackRequest request = new FeedbackRequest("user_001", 3);

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid feedback request"))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void submitFeedback_shouldReturnValidationError_whenRatingOutOfRange() throws Exception {
        FeedbackRequest request = new FeedbackRequest("user_001", 6);

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rating must be between 1 and 5."))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.success").value(false));
    }
}
