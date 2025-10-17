package com.chatapp.demo;

import com.chatapp.demo.model.Candidate;
import com.chatapp.demo.model.Content;
import com.chatapp.demo.model.GeminiResponse;
import com.chatapp.demo.model.Part;
import com.chatapp.demo.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ChatService.
 * Uses Mockito and ExchangeFunction to mock WebClient communication with the external API.
 */
public class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private WebClient.Builder webClientBuilder;

    private ExchangeFunction exchangeFunction;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String MOCK_API_KEY = "mock_key";
    private final String MOCK_MODEL_NAME = "gemini-2.5-flash";
    private final String MOCK_BASE_URL = "https://mock-api.com/models/";
    private final String MOCK_URL;

    public ChatServiceTest() {
        MOCK_URL = MOCK_BASE_URL + MOCK_MODEL_NAME + ":generateContent?key=" + MOCK_API_KEY;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        exchangeFunction = mock(ExchangeFunction.class);

        WebClient mockWebClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        when(webClientBuilder.build()).thenReturn(mockWebClient);

        chatService = new ChatService(webClientBuilder);

        ReflectionTestUtils.setField(chatService, "apiKey", MOCK_API_KEY);
        ReflectionTestUtils.setField(chatService, "modelName", MOCK_MODEL_NAME);
        ReflectionTestUtils.setField(chatService, "baseUrl", MOCK_BASE_URL);

        ReflectionTestUtils.setField(chatService, "chatHistory", new ArrayList<>());
    }

    /**
     * Simplified helper to create ClientResponse for ExchangeFunction.
     * Simulates a response containing the GeminiResponse JSON.
     */
    private ClientResponse mockSuccessResponse(String responseBody) throws Exception {
        GeminiResponse geminiResponse = objectMapper.readValue(responseBody, GeminiResponse.class);

        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.statusCode()).thenReturn(HttpStatus.OK);

        when(clientResponse.bodyToMono(GeminiResponse.class)).thenReturn(Mono.just(geminiResponse));

        when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.empty());

        return clientResponse;
    }

    /**
     * Simplified helper to create ClientResponse for ExchangeFunction in case of an error.
     * Simulates an HTTP error and returns the error body as a String.
     */
    private ClientResponse mockErrorResponse(HttpStatus status, String errorBody) {
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.statusCode()).thenReturn(status);

        when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.just(errorBody));

        when(clientResponse.bodyToMono(GeminiResponse.class)).thenReturn(Mono.error(new RuntimeException("API error occurred")));

        return clientResponse;
    }


    @Test
    void shouldReturnBotResponseAndLogHistoryOnSuccess() throws Exception {
        // Arrange
        String expectedBotResponse = "Odpowiedź na zapytanie użytkownika.";

        Part part = new Part();
        part.setText(expectedBotResponse);

        Content content = new Content();
        content.setParts(List.of(part));

        Candidate candidate = new Candidate();
        candidate.setContent(content);

        GeminiResponse mockResponse = new GeminiResponse();
        mockResponse.setCandidates(List.of(candidate));

        String responseBody = objectMapper.writeValueAsString(mockResponse);

        ClientResponse mockResponse200 = mockSuccessResponse(responseBody);
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(mockResponse200));


        // Act
        String userMessage = "Jak działa mockowanie?";
        String actualResponse = chatService.getGeminiResponse(userMessage);

        // Assert
        assertEquals(expectedBotResponse, actualResponse, "Odpowiedź bota powinna być poprawnie sparsowana.");

        List<Content> history = (List<Content>) ReflectionTestUtils.getField(chatService, "chatHistory");
        assertEquals(2, history.size(), "Historia powinna mieć 2 wiadomości.");
        assertEquals(userMessage, history.get(0).getParts().get(0).getText(), "Pierwsza wiadomość powinna być wiadomością użytkownika.");
        assertEquals(expectedBotResponse, history.get(1).getParts().get(0).getText(), "Druga wiadomość powinna być odpowiedzią bota.");
    }

    @Test
    void shouldReturnErrorOnApiHttpError() throws Exception {
        // Arrange
        String errorBody = "{\"error\": \"API key is invalid\"}";

        ClientResponse mockResponse401 = mockErrorResponse(HttpStatus.UNAUTHORIZED, errorBody);
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(mockResponse401));

        // Act
        String actualResponse = chatService.getGeminiResponse("Test błędu 401");

        // Assert
        String expectedErrorMessage = "Wystąpił błąd serwera podczas komunikacji z AI. Sprawdź klucz API i logi.";
        assertEquals(expectedErrorMessage, actualResponse, "Powinien zostać zwrócony ogólny komunikat o błędzie serwera.");

        List<Content> history = (List<Content>) ReflectionTestUtils.getField(chatService, "chatHistory");
        assertEquals(0, history.size(), "Historia powinna być pusta po błędzie API.");
    }

    @Test
    void shouldReturnErrorOnEmptyCandidates() throws Exception {
        // Arrange
        GeminiResponse mockResponse = new GeminiResponse();
        mockResponse.setCandidates(List.of());
        String responseBody = objectMapper.writeValueAsString(mockResponse);

        ClientResponse mockResponse200Empty = mockSuccessResponse(responseBody);
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(mockResponse200Empty));

        // Act
        String actualResponse = chatService.getGeminiResponse("Pytanie bez odpowiedzi");

        // Assert
        String expectedErrorMessage = "Błąd: Nie udało się uzyskać poprawnej odpowiedzi od API.";
        assertEquals(expectedErrorMessage, actualResponse, "Powinien zostać zwrócony błąd braku poprawnej odpowiedzi.");

        List<Content> history = (List<Content>) ReflectionTestUtils.getField(chatService, "chatHistory");
        assertEquals(0, history.size(), "Historia powinna być pusta.");
    }
}
