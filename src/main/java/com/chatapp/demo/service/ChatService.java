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

    // Zmieniono na WebClient
    private final WebClient webClient;

    // Historia rozmowy (uproszczona implementacja w pamięci). Już NIE zawiera instrukcji systemowej.
    private final List<Content> chatHistory = new ArrayList<>();

    // Instrukcja systemowa przechowywana jako String
    private final String systemPrompt = "You are a helpful and friendly chatbot. Respond concisely in Polish.";

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String modelName;

    @Value("${gemini.api.url}")
    private String baseUrl;

    // Zmieniono konstruktor na użycie WebClient.Builder
    public ChatService(WebClient.Builder webClientBuilder) {
        // Używamy webClientBuilder do utworzenia WebClient
        this.webClient = webClientBuilder.build();
    }

    /**
     * Wysyła zapytanie do Gemini API i zwraca odpowiedź.
     * Dodaje nowe wiadomości do historii rozmowy.
     */
    public String getGeminiResponse(String userMessage) {
        // 1. Przygotowanie ciała żądania z historią rozmowy i instrukcją systemową
        // Przekazujemy systemPrompt do konstruktora, który teraz umieszcza go w GeminiRequest jako systemInstruction
        GeminiRequest requestBody = new GeminiRequest(userMessage, chatHistory, systemPrompt);

        String url = baseUrl + modelName + ":generateContent?key=" + apiKey;

        try {
            // 2. Wykonanie żądania POST za pomocą WebClient (używamy .block() w synchronicznym Kontrolerze)
            GeminiResponse geminiResponse = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                // Logowanie szczegółowego błędu JSON
                                System.err.println("API Error Response Body: " + errorBody);
                                return reactor.core.publisher.Mono.error(new RuntimeException("API zwróciło błąd: " + clientResponse.statusCode() + ", Ciało błędu: " + errorBody));
                            })
                    )
                    .bodyToMono(GeminiResponse.class)
                    .block(); // Blokujemy, aby pasowało do synchronicznej sygnatury metody

            // 3. Przetwarzanie odpowiedzi
            if (geminiResponse != null && geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                String botResponse = geminiResponse.getCandidates().stream()
                        .map(c -> c.getContent().getParts().stream()
                                .map(Part::getText)
                                .collect(Collectors.joining()))
                        .collect(Collectors.joining());

                // 4. Aktualizacja historii rozmowy (tylko "user" i "model")
                // Dodaj wiadomość użytkownika
                chatHistory.add(Content.builder().role("user").parts(List.of(new Part(userMessage))).build());
                // Dodaj odpowiedź bota
                chatHistory.add(Content.builder().role("model").parts(List.of(new Part(botResponse))).build());

                return botResponse;
            }

            return "Błąd: Nie udało się uzyskać poprawnej odpowiedzi od API.";

        } catch (Exception e) {
            // Logowanie szczegółów błędu
            System.err.println("Błąd komunikacji z API: " + e.getMessage());
            return "Wystąpił błąd serwera podczas komunikacji z AI. Sprawdź klucz API i logi.";
        }
    }
}
