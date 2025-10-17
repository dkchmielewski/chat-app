package com.chatapp.demo.controller;

import com.chatapp.demo.model.ChatRequest;
import com.chatapp.demo.model.ChatResponse;
import com.chatapp.demo.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kontroler REST do obsługi punktu końcowego chata.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Endpoint do wysyłania wiadomości i otrzymywania odpowiedzi od chatbota.
     * Użyj: POST http://localhost:8080/api/chat
     * Body (JSON): {"message": "Twoja wiadomość"}
     */
    @PostMapping
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return new ChatResponse("Proszę podać wiadomość.");
        }

        String aiResponse = chatService.getGeminiResponse(request.getMessage());
        return new ChatResponse(aiResponse);
    }
}
