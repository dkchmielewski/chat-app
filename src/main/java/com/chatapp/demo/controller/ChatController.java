package com.chatapp.demo.controller;

import com.chatapp.demo.model.ChatRequest;
import com.chatapp.demo.model.ChatResponse;
import com.chatapp.demo.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller to handle the chat endpoint.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Endpoint for sending messages and receiving a response from the chatbot.
     * Use: POST http://localhost:8080/api/chat
     * Body (JSON): {"message": "Your message"}
     */
    @PostMapping
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return new ChatResponse("Please provide a message.");
        }

        String aiResponse = chatService.getGeminiResponse(request.getMessage());
        return new ChatResponse(aiResponse);
    }
}
