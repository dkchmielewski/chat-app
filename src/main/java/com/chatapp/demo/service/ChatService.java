package com.chatapp.demo.service;

import com.chatapp.demo.model.Content;
import com.chatapp.demo.model.GeminiRequest;
import com.chatapp.demo.model.GeminiResponse;
import com.chatapp.demo.model.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis do zarządzania logiką komunikacji z Gemini API.
 */
@Service
public class ChatService {

    private final WebClient webClient;

    // Chat history (simplified in-memory implementation). NO longer includes system instruction.
    private final List<Content> chatHistory = new ArrayList<>();

    // System instruction stored as a String
    private final String systemPrompt = "You are a helpful and friendly chatbot. Respond concisely in Polish.";

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String modelName;

    @Value("${gemini.api.url}")
    private String baseUrl;

    public ChatService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Wysyła zapytanie do Gemini API i zwraca odpowiedź.
     * Dodaje nowe wiadomości do historii rozmowy.
     */
    public String getGeminiResponse(String userMessage) {
        // 1. Preparing the request body with chat history and system instruction
        // We pass systemPrompt to the constructor, which now places it in GeminiRequest as systemInstruction
        GeminiRequest requestBody = new GeminiRequest(userMessage, chatHistory, systemPrompt);

        String url = baseUrl + modelName + ":generateContent?key=" + apiKey;

        try {
            // 2. Executing the POST request using WebClient (we use .block() in a synchronous Controller)
            GeminiResponse geminiResponse = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                System.err.println("API Error Response Body: " + errorBody);
                                return reactor.core.publisher.Mono.error(new RuntimeException("API zwróciło błąd: " + clientResponse.statusCode() + ", Ciało błędu: " + errorBody));
                            })
                    )
                    .bodyToMono(GeminiResponse.class)
                    .block(); // Blocking to fit the synchronous method signature

            // 3. Processing the response
            if (geminiResponse != null && geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                String botResponse = geminiResponse.getCandidates().stream()
                        .map(c -> c.getContent().getParts().stream()
                                .map(Part::getText)
                                .collect(Collectors.joining()))
                        .collect(Collectors.joining());

                // 4. Updating the chat history (only "user" and "model")
                // Add user message
                chatHistory.add(Content.builder().role("user").parts(List.of(new Part(userMessage))).build());
                // Add bot response
                chatHistory.add(Content.builder().role("model").parts(List.of(new Part(botResponse))).build());

                return botResponse;
            }

            return "Błąd: Nie udało się uzyskać poprawnej odpowiedzi od API.";

        } catch (Exception e) {
            System.err.println("Błąd komunikacji z API: " + e.getMessage());
            return "Wystąpił błąd serwera podczas komunikacji z AI. Sprawdź klucz API i logi.";
        }
    }
}
