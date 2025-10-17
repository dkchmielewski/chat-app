package com.chatapp.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
// Zmieniono import z RestTemplate na WebClient
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main Spring Boot application class.
 */
@SpringBootApplication
public class ChatbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatbotApplication.class, args);
	}

	/**
	 * Configuration for the WebClient.Builder bean used to create WebClient.
	 */
	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

	/**
	 * Configuration for CORS (Cross-Origin Resource Sharing).
	 * Allows the UI (e.g., running on a different port) to communicate with the API.
	 */
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				// Allow access to /api/chat from any source for demo simplicity
				registry.addMapping("/api/**")
						.allowedOrigins("*")
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
			}
		};
	}
}


