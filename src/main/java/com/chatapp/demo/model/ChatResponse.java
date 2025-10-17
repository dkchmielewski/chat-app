package com.chatapp.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Model DTO dla odpowiedzi od chatbota.
 */
@Data
@AllArgsConstructor
public class ChatResponse {
    private String response;
}
