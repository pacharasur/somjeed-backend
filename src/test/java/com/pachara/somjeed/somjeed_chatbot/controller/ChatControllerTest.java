package com.pachara.somjeed.somjeed_chatbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachara.somjeed.somjeed_chatbot.model.request.ChatRequest;
import com.pachara.somjeed.somjeed_chatbot.model.response.ChatResponse;
import com.pachara.somjeed.somjeed_chatbot.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @Test
    void chat_shouldReturnOk_whenRequestValid() throws Exception {
        when(chatService.handle(any(ChatRequest.class)))
                .thenReturn(new ChatResponse(List.of("Good morning, on a sunshine day!")));

        ChatRequest request = new ChatRequest("user_001", "hello");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0]").value("Good morning, on a sunshine day!"));
    }

    @Test
    void chat_shouldReturnBadRequest_whenRequestInvalid() throws Exception {
        when(chatService.handle(any(ChatRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid chat request"));

        ChatRequest request = new ChatRequest("user_001", "hello");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid chat request"))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void chat_shouldReturnValidationError_whenRequiredFieldsMissing() throws Exception {
        ChatRequest request = new ChatRequest("", "");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.success").value(false));
    }
}
