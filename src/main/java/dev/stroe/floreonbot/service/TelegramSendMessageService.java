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
public class TelegramSendMessageService {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";
    private final RestTemplate restTemplate;
    private final String botToken;

    public TelegramSendMessageService(@Value("${telegram.bot.token}") String botToken, RestTemplate restTemplate) {
        this.botToken = botToken;
        this.restTemplate = restTemplate;
    }

    private Map<String, String> createMessageBody(String chatId, String message, Long replyToMessageId) {
        Map<String, String> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", message);
        if (replyToMessageId != null) {
            body.put("reply_to_message_id", String.valueOf(replyToMessageId));
        }
        return body;
    }

    public void sendMessage(Long chatId, String message, Long replyToMessageId) {
        URI uri = UriComponentsBuilder.fromUriString(String.format(TELEGRAM_API_URL, botToken))
                .build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = createMessageBody(String.valueOf(chatId), message, replyToMessageId);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Error sending message: Telegram API returned status " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            System.err.println("Telegram API error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            System.err.println("Error sending message: " + ex.getMessage());
        }
    }
}

