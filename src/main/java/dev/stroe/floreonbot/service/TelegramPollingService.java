package dev.stroe.floreonbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramPollingService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final TelegramUpdateHandlerService updateHandler;
    private long lastUpdateId = 0;

    public TelegramPollingService(TelegramUpdateHandlerService updateHandler) {
        this.updateHandler = updateHandler;
    }

    public long getLastUpdateId(ResponseEntity<String> response) {
        long lastUpdateId = 0;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode resultNode = root.path("result");
            if (resultNode.isArray()) {
                for (JsonNode update : resultNode) {
                    long updateId = update.path("update_id").asLong();
                    // Ensure we always keep the highest update_id
                    if (updateId > lastUpdateId) {
                        lastUpdateId = updateId;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastUpdateId;
    }

    @PostConstruct
    public void startLongPolling() {
        new Thread(() -> {
            while (true) {
                try {
                    // Set timeout to 30 seconds for long polling
                    String url = "https://api.telegram.org/bot" + botToken
                            + "/getUpdates?offset=" + (lastUpdateId + 1)
                            + "&timeout=30";

                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(response.getBody());
                    JsonNode resultNode = rootNode.path("result");

                    if (resultNode.isArray() && !resultNode.isEmpty()) {
                        for (JsonNode update : resultNode) {
                            // Delegate update processing to the handler
                            updateHandler.processUpdate(update);
                        }
                        // Update lastUpdateId
                        lastUpdateId = getLastUpdateId(response);
                    }

                } catch (Exception e) {
                    // Handle exceptions (network issues, parsing errors, etc.)
                    e.printStackTrace();
                    try {
                        // Optional: pause briefly before retrying
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }
}