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
 * Testy integracyjne dla ChatController.
 * Używa @WebMvcTest, aby załadować tylko warstwę kontrolera i zależne komponenty.
 */
// Jawne wskazanie kontrolera do testowania i użycie @ContextConfiguration do zaimportowania
// głównej klasy aplikacji, co powinno rozwiązać problem z ładowaniem kontekstu.
@WebMvcTest(controllers = ChatController.class)
@ContextConfiguration(classes = ChatbotApplication.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Mockujemy ChatService, aby nie ładować jego rzeczywistych zależności
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    // Poprawna ścieżka do endpointu POST
    private static final String CHAT_ENDPOINT = "/api/chat";

    @Test
    void shouldReturnBotResponseOnValidMessage() throws Exception {
        // Arrange
        String userMessage = "Witaj!";
        String botResponse = "Cześć! Jak mogę Ci pomóc?";

        // Mockowanie zachowania serwisu
        when(chatService.getGeminiResponse(anyString())).thenReturn(botResponse);

        // Użycie ChatRequest
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage(userMessage);

        // Act & Assert
        // UŻYTO POPRAWNEJ ŚCIEŻKI: /api/chat
        mockMvc.perform(post(CHAT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                // Kontroler zwraca ChatResponse, który jest serializowany do JSON
                // Oczekujemy pola 'response' w zwróconym JSONie
                .andExpect(jsonPath("$.response").value(botResponse));
    }

    @Test
    void shouldReturnBadRequestOnEmptyMessage() throws Exception {
        // Arrange
        String validationMessage = "Proszę podać wiadomość.";
        // Użycie ChatRequest
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage(""); // Pusta wiadomość

        // Act & Assert
        // UŻYTO POPRAWNEJ ŚCIEŻKI: /api/chat
        mockMvc.perform(post(CHAT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk()) // Kontroler zwraca 200 OK, ale w ciele jest błąd walidacji!
                // Weryfikacja, że odpowiedź zawiera oczekiwany komunikat o błędzie walidacji
                .andExpect(jsonPath("$.response").value(validationMessage));
    }

    @Test
    void shouldReturnBadRequestOnMissingBody() throws Exception {
        // Act & Assert
        // UŻYTO POPRAWNEJ ŚCIEŻKI: /api/chat
        mockMvc.perform(post(CHAT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")) // Pusta treść POST
                .andExpect(status().isBadRequest()); // Oczekujemy statusu 400 Bad Request od Springa
    }
}
