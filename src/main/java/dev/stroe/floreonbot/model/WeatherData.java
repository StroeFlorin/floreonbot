package dev.stroe.floreonbot.model;

import com.fasterxml.jackson.databind.JsonNode;

public class WeatherData {
    private final JsonNode data;
    
    public WeatherData(JsonNode data) {
        this.data = data;
    }
    
    public JsonNode getData() {
        return data;
    }
}