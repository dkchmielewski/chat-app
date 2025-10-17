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

    // The @JsonProperty annotation generates the "system_instruction" key in SnakeCase
    @JsonProperty("system_instruction")
    private Content systemInstruction;

    // Used for history management (minimal implementation)
    public GeminiRequest(String userMessage, List<Content> history, String systemPrompt) {
        this.contents = new ArrayList<>(history);

        // Creating a Content object for the system instruction
        this.systemInstruction = Content.builder()
                .parts(List.of(Part.builder().text(systemPrompt).build()))
                .build();

        // Adding the current user message
        Content userContent = Content.builder()
                .role("user")
                .parts(List.of(Part.builder().text(userMessage).build()))
                .build();
        this.contents.add(userContent);
    }
}
