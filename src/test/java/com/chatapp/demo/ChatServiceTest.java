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
 * Testy jednostkowe dla ChatService.
 * Używa Mockito i ExchangeFunction do mockowania komunikacji WebClient z zewnętrznym API.
 */
public class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private WebClient.Builder webClientBuilder;

    private ExchangeFunction exchangeFunction; // Użyjemy ExchangeFunction do mockowania WebClient

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Stałe do mockowania wartości @Value
    private final String MOCK_API_KEY = "mock_key";
    private final String MOCK_MODEL_NAME = "gemini-2.5-flash";
    private final String MOCK_BASE_URL = "https://mock-api.com/models/";
    private final String MOCK_URL;

    public ChatServiceTest() {
        // Konstrukcja oczekiwanego URL
        MOCK_URL = MOCK_BASE_URL + MOCK_MODEL_NAME + ":generateContent?key=" + MOCK_API_KEY;
    }

    @BeforeEach
    void setUp() {
        // Inicjalizacja Mockito Mocks
        MockitoAnnotations.openMocks(this);

        // 1. Mockowanie ExchangeFunction
        exchangeFunction = mock(ExchangeFunction.class);

        // 2. Tworzenie mockowanego WebClienta
        WebClient mockWebClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        // 3. Konfiguracja mocka WebClient.Builder, aby zwracał nasz mockowany klient.
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 4. Wstrzyknięcie (ponowna inicjalizacja) serwisu z mockowanym WebClientem
        chatService = new ChatService(webClientBuilder);

        // 5. Użycie ReflectionTestUtils do wstrzyknięcia wartości @Value
        ReflectionTestUtils.setField(chatService, "apiKey", MOCK_API_KEY);
        ReflectionTestUtils.setField(chatService, "modelName", MOCK_MODEL_NAME);
        ReflectionTestUtils.setField(chatService, "baseUrl", MOCK_BASE_URL);

        // 6. Wyczyszczenie historii czatu przed każdym testem
        ReflectionTestUtils.setField(chatService, "chatHistory", new ArrayList<>());
    }

    /**
     * Uproszczony helper do tworzenia ClientResponse dla ExchangeFunction.
     * Symuluje odpowiedź zawierającą JSON GeminiResponse.
     */
    private ClientResponse mockSuccessResponse(String responseBody) throws Exception {
        // Wczytanie JSON do obiektu GeminiResponse
        GeminiResponse geminiResponse = objectMapper.readValue(responseBody, GeminiResponse.class);

        // Mockowanie ClientResponse
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.statusCode()).thenReturn(HttpStatus.OK);

        // Mockowanie bodyToMono, które zwraca sparsowany obiekt
        when(clientResponse.bodyToMono(GeminiResponse.class)).thenReturn(Mono.just(geminiResponse));

        // Upewnienie się, że bodyToMono(String.class) zwraca pusty ciąg (lub pomijamy to, jeśli nie jest używane)
        when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.empty());

        return clientResponse;
    }

    /**
     * Uproszczony helper do tworzenia ClientResponse dla ExchangeFunction w przypadku błędu.
     * Symuluje błąd HTTP i zwraca ciało błędu jako String.
     */
    private ClientResponse mockErrorResponse(HttpStatus status, String errorBody) {
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.statusCode()).thenReturn(status);

        // W przypadku błędu, zwracamy ciało błędu jako String
        when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.just(errorBody));

        // Mockowanie bodyToMono(GeminiResponse.class) na pusty Mono lub błąd
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

        // Użycie ExchangeFunction do symulowania pomyślnej odpowiedzi
        ClientResponse mockResponse200 = mockSuccessResponse(responseBody);
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(mockResponse200));


        // Act
        String userMessage = "Jak działa mockowanie?";
        String actualResponse = chatService.getGeminiResponse(userMessage);

        // Assert
        assertEquals(expectedBotResponse, actualResponse, "Odpowiedź bota powinna być poprawnie sparsowana.");

        // Weryfikacja historii rozmowy (user + model)
        List<Content> history = (List<Content>) ReflectionTestUtils.getField(chatService, "chatHistory");
        assertEquals(2, history.size(), "Historia powinna mieć 2 wiadomości.");
        assertEquals(userMessage, history.get(0).getParts().get(0).getText(), "Pierwsza wiadomość powinna być wiadomością użytkownika.");
        assertEquals(expectedBotResponse, history.get(1).getParts().get(0).getText(), "Druga wiadomość powinna być odpowiedzią bota.");
    }

    @Test
    void shouldReturnErrorOnApiHttpError() throws Exception {
        // Arrange
        String errorBody = "{\"error\": \"API key is invalid\"}";

        // Użycie ExchangeFunction do symulowania błędu HTTP (np. 401 Unauthorized)
        ClientResponse mockResponse401 = mockErrorResponse(HttpStatus.UNAUTHORIZED, errorBody);
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(mockResponse401));

        // Act
        String actualResponse = chatService.getGeminiResponse("Test błędu 401");

        // Assert
        String expectedErrorMessage = "Wystąpił błąd serwera podczas komunikacji z AI. Sprawdź klucz API i logi.";
        assertEquals(expectedErrorMessage, actualResponse, "Powinien zostać zwrócony ogólny komunikat o błędzie serwera.");

        // Weryfikacja: historia powinna pozostać pusta
        List<Content> history = (List<Content>) ReflectionTestUtils.getField(chatService, "chatHistory");
        assertEquals(0, history.size(), "Historia powinna być pusta po błędzie API.");
    }

    @Test
    void shouldReturnErrorOnEmptyCandidates() throws Exception {
        // Arrange
        GeminiResponse mockResponse = new GeminiResponse();
        mockResponse.setCandidates(List.of());
        String responseBody = objectMapper.writeValueAsString(mockResponse);

        // Użycie ExchangeFunction do symulowania pomyślnej odpowiedzi (ale bez kandydatów)
        ClientResponse mockResponse200Empty = mockSuccessResponse(responseBody);
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(mockResponse200Empty));

        // Act
        String actualResponse = chatService.getGeminiResponse("Pytanie bez odpowiedzi");

        // Assert
        String expectedErrorMessage = "Błąd: Nie udało się uzyskać poprawnej odpowiedzi od API.";
        assertEquals(expectedErrorMessage, actualResponse, "Powinien zostać zwrócony błąd braku poprawnej odpowiedzi.");

        // Weryfikacja: historia powinna pozostać pusta
        List<Content> history = (List<Content>) ReflectionTestUtils.getField(chatService, "chatHistory");
        assertEquals(0, history.size(), "Historia powinna być pusta.");
    }
}
