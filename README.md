Gemini Chatbot (Spring Boot + WebClient + HTML/React)

This project implements a simple chatbot application that uses the Gemini API for response generation. The backend is built on the Spring Boot framework using WebClient, while the user interface is provided by a static HTML/JavaScript file or a React component served by Spring.

-----------------------------------------------------------------------------------------------------------------------

⚙️ Requirements

Java 17+

Maven or Gradle (The project structure uses Maven)

Gemini API Key: Necessary for the service to function.

-----------------------------------------------------------------------------------------------------------------------

🚀 Getting Started


1. API Key Configuration

The Spring Boot application requires a Gemini API key. You must configure it in the src/main/resources/application.yml file:

gemini:
    - api:
    - key: "YOUR_GEMINI_API_KEY" # Replace this string with your actual key
    - model: "gemini-2.5-flash"


Security Note: It is strongly recommended to use environment variables to store API keys instead of placing them directly in configuration files.


2. Running the Backend (Spring Boot)

Navigate to the project's root directory:

`cd chat-app`

Build the project using Maven:

`mvn clean install`


Run the application:

`mvn spring-boot:run`


The backend application will start on port 8080 by default.


3. Accessing the Front-end

Once the Spring Boot server is running:

The chat interface will be available at: http://localhost:8080/

Spring Boot automatically serves the static index.html file (or the alternative ChatApp.jsx component) from the src/main/resources/static/ directory.

-----------------------------------------------------------------------------------------------------------------------

🧪 Testing

The project includes unit and integration tests that can be run using Maven.

Backend (Java)

Tests for the Controller (ChatControllerTests) and Service (ChatServiceTests):

mvn test