package dev.stroe.floreonbot.command;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stroe.floreonbot.model.Location;
import dev.stroe.floreonbot.model.WeatherData;
import dev.stroe.floreonbot.service.GeocodingService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;
import dev.stroe.floreonbot.service.WeatherService;

@Component
public class WeatherForecastCommand implements Command {
    private final TelegramSendMessageService messageService;
    private final GeocodingService geocodingService;
    private final WeatherService weatherService;
    private String command = "";

    public WeatherForecastCommand(
            TelegramSendMessageService telegramSendMessageService,
            GeocodingService geocodingService,
            WeatherService weatherService) {
        this.messageService = telegramSendMessageService;
        this.geocodingService = geocodingService;
        this.weatherService = weatherService;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        if (text == null || text.trim().isEmpty()) {
            messageService.sendMessage(chatId, "Please provide a location. Usage: /" + commandName + " [city name]",
                    messageId);
            return;
        }

        command = commandName;

        try {
            // 1. Get coordinates from location name
            Location location = geocodingService.getCoordinates(text.trim());
            if (location == null) {
                messageService.sendMessage(chatId, "Location not found. Please try a different name.", messageId);
                return;
            }

            // 2. Get weather forecast using coordinates
            WeatherData weatherData = weatherService.getWeatherForecast(location);

            // 3. Format and send the message based on command
            String weatherMessage = formatWeatherMessage(weatherData, location.getFormattedName(), commandName);
            messageService.sendMessage(chatId, weatherMessage, messageId);

        } catch (IOException | InterruptedException e) {
            messageService.sendMessage(chatId, "Error fetching weather data: " + e.getMessage(), messageId);
            return;
        }
    }

    private String formatWeatherMessage(WeatherData weatherData, String location, String commandName) {
        JsonNode data = weatherData.getData();
        switch (commandName) {
            case "temperature":
                return formatTemperatureMessage(data, location);
            case "meteo":
            case "weather":
                return formatCurrentWeatherMessage(data, location);
            case "weatherforecast":
            default:
                return formatFullForecastMessage(data, location);
        }
    }

    private String formatTemperatureMessage(JsonNode weatherData, String location) {
        StringBuilder message = new StringBuilder();
        JsonNode current = weatherData.get("current");

        int currentTemp = (int) Math.round(current.get("temperature_2m").asDouble());
        int feelsLikeTemp = (int) Math.round(current.get("apparent_temperature").asDouble());

        message.append("Temperature in ").append(location).append(":\n\n");
        message.append("🌡️ Current: ").append(currentTemp).append("°C\n");
        message.append("🌡️ Feels like: ").append(feelsLikeTemp).append("°C");

        return message.toString();
    }

    private String formatCurrentWeatherMessage(JsonNode weatherData, String location) {
        StringBuilder message = new StringBuilder();
        message.append("Current Weather in ").append(location).append(":\n\n");

        JsonNode current = weatherData.get("current");
        int currentWeatherCode = current.get("weather_code").asInt();
        String weatherDescription = weatherService.getWeatherDescription(currentWeatherCode);

        int currentTemp = (int) Math.round(current.get("temperature_2m").asDouble());
        int feelsLikeTemp = (int) Math.round(current.get("apparent_temperature").asDouble());
        int humidity = current.get("relative_humidity_2m").asInt();
        int windSpeed = (int) Math.round(current.get("wind_speed_10m").asDouble());

        JsonNode daily = weatherData.get("daily");
        String sunrise = daily.get("sunrise").get(0).asText().substring(11, 16);
        String sunset = daily.get("sunset").get(0).asText().substring(11, 16);
        int todayPrecipitationChance = daily.get("precipitation_probability_max").get(0).asInt();

        message.append(weatherDescription).append("\n");
        message.append("🌡️ Temperature: ").append(currentTemp).append("°C ");
        message.append("(feels like: ").append(feelsLikeTemp).append("°C)\n");
        message.append("💧 Humidity: ").append(humidity).append("%\n");
        message.append("💨 Wind: ").append(windSpeed).append(" km/h\n");
        message.append("🌧️ Precipitation probability: ").append(todayPrecipitationChance).append("%\n");
        message.append("🌅 Sunrise: ").append(sunrise).append("\n");
        message.append("🌇 Sunset: ").append(sunset);

        return message.toString();
    }

    private String formatFullForecastMessage(JsonNode weatherData, String location) {
        StringBuilder message = new StringBuilder();
        message.append("Weather Forecast for ").append(location).append(" \n\n");

        JsonNode daily = weatherData.get("daily");
        JsonNode times = daily.get("time");
        JsonNode weatherCodes = daily.get("weather_code");
        JsonNode maxTemps = daily.get("temperature_2m_max");
        JsonNode minTemps = daily.get("temperature_2m_min");
        JsonNode precipProbability = daily.get("precipitation_probability_max");

        message.append("7-Day Forecast:\n\n");

        for (int i = 0; i < times.size(); i++) {
            LocalDate date = LocalDate.parse(times.get(i).asText());
            String dayName = i == 0 ? "Today" : date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            int code = weatherCodes.get(i).asInt();
            String dayWeatherDesc = weatherService.getWeatherDescription(code);
            int minTemp = (int) Math.round(minTemps.get(i).asDouble());
            int maxTemp = (int) Math.round(maxTemps.get(i).asDouble());
            int precipitationChance = precipProbability.get(i).asInt();

            message.append("• ").append(dayName).append(": ");
            message.append(dayWeatherDesc).append(", ");
            message.append("🌡️ ").append(minTemp).append("°C to ");
            message.append(maxTemp).append("°C, ");
            message.append("🌧️ ").append(precipitationChance).append("%\n\n");
        }

        return message.toString();
    }

    @Override
    public String getDescription() {
        return getDescriptionForCommand(command);
    }

    public String getDescriptionForCommand(String commandName) {
        switch (commandName) {
            case "temperature":
                return "Get the current temperature for a location.";
            case "meteo":
                return "Get the current weather for a location.";
            case "weather":
                return "Get the current weather for a location.";
            case "forecast":
                return "Get a 7-day weather forecast for a location.";
            default:
                return "Get weather information for a location.";
        }
    }
}
