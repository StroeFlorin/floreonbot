package dev.stroe.floreonbot.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class TelegramSendChatAction {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendChatAction";
    private final RestTemplate restTemplate;
    private final String botToken;

    public TelegramSendChatAction(@Value("${telegram.bot.token}") String botToken, RestTemplate restTemplate) {
        this.botToken = botToken;
        this.restTemplate = restTemplate;
    }

    private Map<String, String> createActionBody(String chatId) {
        Map<String, String> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("action", "typing");
        return body;
    }

    /**
     * Sends a typing action to the specified chat to show the bot is typing
     * @param chatId The chat identifier
     */
    public void sendTypingAction(Long chatId) {
        URI uri = UriComponentsBuilder.fromUriString(String.format(TELEGRAM_API_URL, botToken))
                .build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = createActionBody(String.valueOf(chatId));
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Error sending typing action: Telegram API returned status " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            System.err.println("Telegram API error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            System.err.println("Error sending typing action: " + ex.getMessage());
        }
    }
}

