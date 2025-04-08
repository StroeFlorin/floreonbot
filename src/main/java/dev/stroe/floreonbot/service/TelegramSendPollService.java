package dev.stroe.floreonbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class TelegramSendPollService {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendPoll";
    private final RestTemplate restTemplate;
    private final String botToken;

    public TelegramSendPollService(@Value("${telegram.bot.token}") String botToken, RestTemplate restTemplate) {
        this.botToken = botToken;
        this.restTemplate = restTemplate;
    }

    private Map<String, Object> createPollBody(String chatId, String question, List<String> options) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("question", question);
        body.put("options", options);
        body.put("is_anonymous", false);
        return body;
    }

    public void sendPoll(Long chatId, String question, List<String> options) {
        URI uri = UriComponentsBuilder.fromUriString(String.format(TELEGRAM_API_URL, botToken))
                .build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> body = createPollBody(String.valueOf(chatId), question, options);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Error sending poll: Telegram API returned status " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            System.err.println("Telegram API error while sending poll: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            System.err.println("Error sending poll: " + ex.getMessage());
        }
    }
}

