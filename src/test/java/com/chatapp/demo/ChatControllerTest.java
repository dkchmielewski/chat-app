package com.chatapp.demo;

import com.chatapp.demo.controller.ChatController;
import com.chatapp.demo.model.ChatRequest;
import com.chatapp.demo.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration; // Dodano import dla ContextConfiguration
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ChatController.
 * Uses @WebMvcTest to load only the controller layer and dependent components.
 */
@WebMvcTest(controllers = ChatController.class)
@ContextConfiguration(classes = ChatbotApplication.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CHAT_ENDPOINT = "/api/chat";

    @Test
    void shouldReturnBotResponseOnValidMessage() throws Exception {
        String userMessage = "Witaj!";
        String botResponse = "Cześć! Jak mogę Ci pomóc?";

        when(chatService.getGeminiResponse(anyString())).thenReturn(botResponse);

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage(userMessage);

        mockMvc.perform(post(CHAT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(botResponse));
    }

    @Test
    void shouldReturnBadRequestOnEmptyMessage() throws Exception {
        String validationMessage = "Please provide a message.";

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage("");

        mockMvc.perform(post(CHAT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(validationMessage));
    }

    @Test
    void shouldReturnBadRequestOnMissingBody() throws Exception {

        mockMvc.perform(post(CHAT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
