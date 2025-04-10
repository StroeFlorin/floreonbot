package dev.stroe.floreonbot.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.stroe.floreonbot.model.Location;
import dev.stroe.floreonbot.model.WeatherData;

@Service
public class OpenMeteoWeatherService implements WeatherService {
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<Integer, String> weatherCodeMap;
    
    public OpenMeteoWeatherService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.weatherCodeMap = initWeatherCodeMap();
    }
    
    @Override
    public WeatherData getWeatherForecast(Location location) throws IOException, InterruptedException {
        String urlString = String.format("%s?latitude=%.4f&longitude=%.4f&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,apparent_temperature&timezone=auto", 
                WEATHER_API_URL, location.getLatitude(), location.getLongitude());
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Weather API returned status code: " + response.statusCode());
        }

        return new WeatherData(objectMapper.readTree(response.body()));
    }
    
    @Override
    public String getWeatherDescription(int weatherCode) {
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
}