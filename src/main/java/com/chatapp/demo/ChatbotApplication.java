package com.chatapp.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
// Zmieniono import z RestTemplate na WebClient
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Główna klasa aplikacji Spring Boot.
 */
@SpringBootApplication
public class ChatbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatbotApplication.class, args);
	}

	/**
	 * Konfiguracja beana WebClient.Builder do tworzenia WebClient.
	 */
	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

	/**
	 * Konfiguracja CORS (Cross-Origin Resource Sharing).
	 * Pozwala UI (działającemu np. na innym porcie) komunikować się z API.
	 */
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				// Pozwól na dostęp do /api/chat z dowolnego źródła dla prostoty demo
				registry.addMapping("/api/**")
						.allowedOrigins("*")
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
			}
		};
	}
}

// --- DTO dla komunikacji z Gemini API ---

// USUNIĘTO GenerationConfig, ponieważ API go nie akceptuje w ten sposób


