package com.pachara.somjeed.somjeed_chatbot.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "User ID is required.")
    private String userId;

    @NotBlank(message = "Message is required.")
    private String message;
}
