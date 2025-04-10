package dev.stroe.floreonbot.service;

import java.io.IOException;
import dev.stroe.floreonbot.model.WeatherData;
import dev.stroe.floreonbot.model.Location;

public interface WeatherService {
    WeatherData getWeatherForecast(Location location) throws IOException, InterruptedException;
    String getWeatherDescription(int weatherCode);
}