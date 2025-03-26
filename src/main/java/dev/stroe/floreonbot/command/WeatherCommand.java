package dev.stroe.floreonbot.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.stroe.floreonbot.service.TelegramSendMessageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class WeatherCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(WeatherCommand.class);
    private static final String GEOCODING_URL = "http://api.openweathermap.org/geo/1.0/direct";
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TelegramSendMessageService messageService;

    public WeatherCommand(
            @Value("${openweather.api.key}") String apiKey,
            TelegramSendMessageService telegramSendMessageService) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.messageService = telegramSendMessageService;
    }

    @Override
    public void execute(String text, Long chatId, Long userId, Long messageId) {
        if (text == null || text.trim().isEmpty()) {
            messageService.sendMessage(chatId, "Please provide a location. Usage: /weather [city name]", messageId);
            return;
        }

        try {
            // 1. Get coordinates from location name
            JsonNode geoData = getCoordinates(text.trim());
            if (geoData == null || geoData.size() == 0) {
                messageService.sendMessage(chatId, "Location not found. Please try a different name.", messageId);
                return;
            }

            // Extract lat and lon from the first result
            JsonNode firstLocation = geoData.get(0);
            double lat = firstLocation.get("lat").asDouble();
            double lon = firstLocation.get("lon").asDouble();
            String locationName = firstLocation.get("name").asText();
            String country = firstLocation.get("country").asText();

            // 2. Get weather data from coordinates
            JsonNode weatherData = getWeather(lat, lon);
            
            // 3. Format the response
            String weatherMessage = formatWeatherResponse(weatherData, locationName, country);

            // 4. Send the response
            messageService.sendMessage(chatId, weatherMessage, messageId);

        } catch (IOException e) {
            logger.error("Error accessing weather service", e);
            messageService.sendMessage(chatId, "Error accessing weather service: " + e.getMessage(), messageId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Weather request interrupted", e);
            messageService.sendMessage(chatId, "Weather request was interrupted. Please try again.", messageId);
        } catch (Exception e) {
            logger.error("Unexpected error in weather command", e);
            messageService.sendMessage(chatId, "An unexpected error occurred: " + e.getMessage(), messageId);
        }
    }

    private JsonNode getCoordinates(String location) throws IOException, InterruptedException {
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String urlString = String.format("%s?q=%s&limit=1&appid=%s", 
                GEOCODING_URL, encodedLocation, apiKey);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Geocoding API returned status code: " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    private JsonNode getWeather(double lat, double lon) throws IOException, InterruptedException {
        String urlString = String.format("%s?lat=%f&lon=%f&units=metric&appid=%s", 
                WEATHER_URL, lat, lon, apiKey);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Weather API returned status code: " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    private String formatWeatherResponse(JsonNode weatherData, String locationName, String country) {
        // Main weather information
        JsonNode main = weatherData.get("main");
        JsonNode weather = weatherData.get("weather").get(0);
        
        double temp = main.get("temp").asDouble();
        double feelsLike = main.get("feels_like").asDouble();
        int humidity = main.get("humidity").asInt();
        String description = weather.get("description").asText();
        
        // Wind information
        double windSpeed = weatherData.get("wind").get("speed").asDouble();
        
        // Sunrise and Sunset
        JsonNode sys = weatherData.get("sys");
        long sunriseUnix = sys.get("sunrise").asLong();
        long sunsetUnix = sys.get("sunset").asLong();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String sunriseStr = LocalDateTime.ofInstant(Instant.ofEpochSecond(sunriseUnix), ZoneId.systemDefault()).format(timeFormatter);
        String sunsetStr = LocalDateTime.ofInstant(Instant.ofEpochSecond(sunsetUnix), ZoneId.systemDefault()).format(timeFormatter);
        
        // Format the response
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🌦 Weather in %s, %s:\n\n", locationName, country));
        sb.append(String.format("🌡 Temperature: %.1f°C, feels like: %.1f°C\n", temp, feelsLike));
        sb.append(String.format("☁️ Conditions: %s\n", description));
        sb.append(String.format("💧 Humidity: %d%%\n", humidity));
        sb.append(String.format("🌬️ Wind: %.1f m/s\n", windSpeed));
        sb.append(String.format("☀️ Sunrise: %s / Sunset: %s\n", sunriseStr, sunsetStr));
        
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "Get current weather for a location.";
    }
}
