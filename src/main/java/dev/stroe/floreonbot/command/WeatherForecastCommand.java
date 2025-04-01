package dev.stroe.floreonbot.command;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class WeatherForecastCommand implements Command {
    private static final String GEOCODING_URL = "http://api.openweathermap.org/geo/1.0/direct";
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast";
    private final TelegramSendMessageService messageService;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<Integer, String> weatherCodeMap;
    
    public WeatherForecastCommand(
            @Value("${openweather.api.key}") String apiKey,
            TelegramSendMessageService telegramSendMessageService) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.messageService = telegramSendMessageService;
        this.weatherCodeMap = initWeatherCodeMap();
    }

    @Override
    public void execute(String text, Long chatId, Long userId, Long messageId) {
        if (text == null || text.trim().isEmpty()) {
            messageService.sendMessage(chatId, "Please provide a location. Usage: /weatherforecast [city name]", messageId);
            return;
        }

        try {
            // 1. Get coordinates from location name
            JsonNode geoData = getCoordinates(text.trim());
            if (geoData == null || geoData.size() == 0) {
                messageService.sendMessage(chatId, "Location not found. Please try a different name.", messageId);
                return;
            }

            JsonNode firstLocation = geoData.get(0);
            double lat = firstLocation.get("lat").asDouble();
            double lon = firstLocation.get("lon").asDouble();
            String locationName = firstLocation.get("name").asText();
            String country = firstLocation.get("country").asText();
            
            // 2. Get weather forecast using coordinates
            JsonNode weatherData = getWeatherForecast(lat, lon);
            
            // 3. Format and send the message
            String weatherMessage = formatWeatherMessage(weatherData, locationName + ", " + country);
            messageService.sendMessage(chatId, weatherMessage, messageId);

        } catch (IOException | InterruptedException e) {
            messageService.sendMessage(chatId, "Error fetching weather data: " + e.getMessage(), messageId);
            return;
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
    
    private JsonNode getWeatherForecast(double latitude, double longitude) throws IOException, InterruptedException {
        String urlString = String.format("%s?latitude=%.4f&longitude=%.4f&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,apparent_temperature&timezone=auto", 
                WEATHER_API_URL, latitude, longitude);
        
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
    
    private String formatWeatherMessage(JsonNode weatherData, String location) {
        StringBuilder message = new StringBuilder();
        message.append("Weather Forecast for ").append(location).append(" \n\n");
        
        // Current weather
        JsonNode current = weatherData.get("current");
        int currentWeatherCode = current.get("weather_code").asInt();
        String weatherDescription = getWeatherDescription(currentWeatherCode);
        
        // Extract current weather data into variables
        double currentTemp = current.get("temperature_2m").asDouble();
        double feelsLikeTemp = current.get("apparent_temperature").asDouble();
        int humidity = current.get("relative_humidity_2m").asInt();
        double windSpeed = current.get("wind_speed_10m").asDouble();
        
        // Get today's data from daily forecast
        JsonNode daily = weatherData.get("daily");
        String sunrise = daily.get("sunrise").get(0).asText().substring(11, 16); // Extract HH:MM
        String sunset = daily.get("sunset").get(0).asText().substring(11, 16);   // Extract HH:MM
        int todayPrecipitationChance = daily.get("precipitation_probability_max").get(0).asInt();
        
        message.append("Current Conditions:\n\n");
        message.append(weatherDescription).append("\n");
        message.append("🌡️ Temperature: ").append(currentTemp).append("°C ");
        message.append("(feels like: ").append(feelsLikeTemp).append("°C)\n");
        message.append("💧 Humidity: ").append(humidity).append("%\n");
        message.append("💨 Wind: ").append(windSpeed).append(" km/h\n");
        message.append("🌧️ Precipitation probability: ").append(todayPrecipitationChance).append("%\n");
        message.append("🌅 Sunrise: ").append(sunrise).append("\n");
        message.append("🌇 Sunset: ").append(sunset).append("\n\n\n");
        
        // Daily forecast
        JsonNode times = daily.get("time");
        JsonNode weatherCodes = daily.get("weather_code");
        JsonNode maxTemps = daily.get("temperature_2m_max");
        JsonNode minTemps = daily.get("temperature_2m_min");
        JsonNode precipProbability = daily.get("precipitation_probability_max");
        
        message.append("7-Day Forecast:\n\n");
        
        for (int i = 0; i < times.size(); i++) {
            // Parse the date and get the day name
            LocalDate date = LocalDate.parse(times.get(i).asText());
            String dayName = i == 0 ? "Today" : date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            
            // Extract daily forecast data into variables
            int code = weatherCodes.get(i).asInt();
            String dayWeatherDesc = getWeatherDescription(code);
            double minTemp = minTemps.get(i).asDouble();
            double maxTemp = maxTemps.get(i).asDouble();
            int precipitationChance = precipProbability.get(i).asInt();
            
            message.append("• ").append(dayName).append(": ");
            message.append(dayWeatherDesc).append(", ");
            message.append("🌡️ ").append(minTemp).append("°C to ");
            message.append(maxTemp).append("°C, ");
            message.append("🌧️ ").append(precipitationChance).append("% precipitation probability\n\n");
        }
        
        return message.toString();
    }
    
    private String getWeatherDescription(int weatherCode) {
        return weatherCodeMap.getOrDefault(weatherCode, "Unknown weather");
    }
    
    private Map<Integer, String> initWeatherCodeMap() {
        Map<Integer, String> codeMap = new HashMap<>();
        // Clear
        codeMap.put(0, "☀️ Clear sky");
        // Partly cloudy
        codeMap.put(1, "🌤️ Mainly clear");
        codeMap.put(2, "🌤️ Partly cloudy");
        codeMap.put(3, "☁️ Overcast");
        // Fog
        codeMap.put(45, "🌫️ Fog");
        codeMap.put(48, "🌫️ Depositing rime fog");
        // Drizzle
        codeMap.put(51, "🌧️ Light drizzle");
        codeMap.put(53, "🌧️ Moderate drizzle");
        codeMap.put(55, "🌧️ Dense drizzle");
        // Rain
        codeMap.put(61, "🌧️ Slight rain");
        codeMap.put(63, "🌧️ Moderate rain");
        codeMap.put(65, "🌧️ Heavy rain");
        codeMap.put(80, "🌧️ Slight rain showers");
        codeMap.put(81, "🌧️ Moderate rain showers");
        codeMap.put(82, "🌧️ Violent rain showers");
        // Snow
        codeMap.put(71, "🌨️ Slight snow fall");
        codeMap.put(73, "🌨️ Moderate snow fall");
        codeMap.put(75, "🌨️ Heavy snow fall");
        codeMap.put(85, "🌨️ Slight snow showers");
        codeMap.put(86, "🌨️ Heavy snow showers");
        // Thunderstorm
        codeMap.put(95, "⛈️ Thunderstorm");
        codeMap.put(96, "⛈️ Thunderstorm with slight hail");
        codeMap.put(99, "⛈️ Thunderstorm with heavy hail");
        
        return codeMap;
    }

    @Override
    public String getDescription() {
        return "Get a 7-day forecast for a location.";
    }
}
