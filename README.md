Chatbot Gemini (Spring Boot + WebClient + React/HTML)


Ten projekt jest aplikacją typu chatbot, która wykorzystuje Gemini API do generowania odpowiedzi. Backend jest zbudowany na platformie Spring Boot z wykorzystaniem WebClient, a interfejs użytkownika jest obsługiwany przez statyczny plik HTML/JavaScript lub komponent React serwowany przez Spring.


⚙️ Wymagania

Java 17+

Maven lub Gradle (projekt używa Maven, zgodnie ze strukturą)

Klucz API Gemini: Niezbędny do działania serwisu.

----------------------------------------------------------------------------------------------------------------

🚀 Uruchomienie aplikacji

1. Konfiguracja klucza API

Aplikacja Spring Boot wymaga klucza API Gemini. Należy go skonfigurować w pliku src/main/resources/application.yml:

gemini:
api-key: "TWOJ_KLUCZ_API_GEMINI" # Zastąp ten ciąg swoim rzeczywistym kluczem
model: "gemini-2.5-flash"


Zdecydowanie zaleca się użycie zmiennych środowiskowych do przechowywania kluczy API, zamiast umieszczania ich bezpośrednio w plikach konfiguracyjnych.


2. Uruchomienie Backendu (Spring Boot)

- Przejdź do katalogu głównego projektu:

`cd chat-app`


- Zbuduj projekt za pomocą Mavena:

`mvn clean install`


- Uruchom aplikację:

`mvn spring-boot:run`


Aplikacja backendowa uruchomi się domyślnie na porcie 8080.


3. Dostęp do Front-endu

Po uruchomieniu serwera Spring Boot:

Interfejs czatu będzie dostępny pod adresem: <bold>http://localhost:8080/</bold>

Spring Boot automatycznie serwuje statyczny plik index.html (lub alternatywnie komponent ChatApp.jsx) z katalogu src/main/resources/static/.

----------------------------------------------------------------------------------------------------------------

🧪 Testy

Projekt zawiera testy jednostkowe i integracyjne, które można uruchomić za pomocą Mavena.

Backend (Java)

Testy kontrolera (ChatControllerTest) i serwisu (ChatServiceTest):

`mvn test`

----------------------------------------------------------------------------------------------------------------

Frontend (React/JSX)

Jeśli projekt korzysta z pełnego środowiska React (z package.json i narzędziami takimi jak Jest i RTL), należy uruchomić testy w katalogu front-endu (jeśli taki istnieje, np. /src/main/frontend).

W tym projekcie plik App.test.jsx jest demonstracją. Aby go uruchomić, potrzebne jest pełne środowisko Node.js/Jest/RTL.



