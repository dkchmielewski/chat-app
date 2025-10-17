package com.chatapp.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class GeminiRequest {
    private List<Content> contents;

    // ZMIANA: Instrukcja systemowa wraca na poziom główny, ale jako obiekt Content
    // Adnotacja @JsonProperty generuje klucz "system_instruction" w SnakeCase
    @JsonProperty("system_instruction")
    private Content systemInstruction;

    // Używane do utrzymania historii (minimalna implementacja)
    public GeminiRequest(String userMessage, List<Content> history, String systemPrompt) {
        this.contents = new ArrayList<>(history);

        // Utworzenie obiektu Content dla instrukcji systemowej
        this.systemInstruction = Content.builder()
                .parts(List.of(Part.builder().text(systemPrompt).build()))
                .build(); // Rola jest pomijana, ponieważ API to pole usuwa/ignoruje

        // Dodanie aktualnej wiadomości użytkownika
        Content userContent = Content.builder()
                .role("user")
                .parts(List.of(Part.builder().text(userMessage).build()))
                .build();
        this.contents.add(userContent);
    }
}
