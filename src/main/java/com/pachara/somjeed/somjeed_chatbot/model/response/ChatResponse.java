package com.pachara.somjeed.somjeed_chatbot.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChatResponse {

    private List<String> messages;

    public ChatResponse(List<String> messages) {
        this.messages = messages;
    }
}
