package com.chatapp.demo.model;

import lombok.Data;

/**
 * Model DTO dla pojedynczego żądania od użytkownika.
 */
@Data
public class ChatRequest {
    private String message;
}
