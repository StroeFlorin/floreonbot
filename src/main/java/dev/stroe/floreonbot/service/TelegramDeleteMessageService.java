package dev.stroe.floreonbot.service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TelegramDeleteMessageService {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/deleteMessages";
    private final RestTemplate restTemplate;
    private final String botToken;

    public TelegramDeleteMessageService(@Value("${telegram.bot.token}") String botToken, RestTemplate restTemplate) {
        this.botToken = botToken;
        this.restTemplate = restTemplate;
    }

    private Map<String, Object> createDeleteMessagesBody(String chatId, List<Long> messageIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("message_ids", messageIds);
        return body;
    }

    public void deleteMessages(Long chatId, List<Long> messageIds) {
        URI uri = UriComponentsBuilder.fromUriString(String.format(TELEGRAM_API_URL, botToken))
                .build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = createDeleteMessagesBody(String.valueOf(chatId), messageIds);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Error deleting messages: Telegram API returned status " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            System.err.println("Telegram API error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            System.err.println("Error deleting messages: " + ex.getMessage());
        }
    }
}
