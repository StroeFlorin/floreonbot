package dev.stroe.floreonbot.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.stroe.floreonbot.model.Location;

@Service
public class OpenWeatherGeocodingService implements GeocodingService {
    private static final String GEOCODING_URL = "http://api.openweathermap.org/geo/1.0/direct";
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public OpenWeatherGeocodingService(@Value("${openweather.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public Location getCoordinates(String locationName) throws IOException, InterruptedException {
        String encodedLocation = URLEncoder.encode(locationName, StandardCharsets.UTF_8);
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

        JsonNode geoData = objectMapper.readTree(response.body());
        
        if (geoData == null || geoData.size() == 0) {
            return null;
        }
        
        JsonNode firstLocation = geoData.get(0);
        return new Location(
            firstLocation.get("lat").asDouble(),
            firstLocation.get("lon").asDouble(),
            firstLocation.get("name").asText(),
            firstLocation.get("country").asText()
        );
    }
}