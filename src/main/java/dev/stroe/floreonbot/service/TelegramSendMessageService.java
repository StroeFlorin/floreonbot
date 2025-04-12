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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TelegramSendMessageService {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";
    private final RestTemplate restTemplate;
    private final String botToken;
    private final ObjectMapper objectMapper;
    private final TelegramUserService telegramUserService;
    private final TelegramChatService telegramChatService;
    private final TelegramMessageService telegramMessageService;

    public TelegramSendMessageService(
            @Value("${telegram.bot.token}") String botToken,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            TelegramUserService telegramUserService,
            TelegramChatService telegramChatService,
            TelegramMessageService telegramMessageService) {
        this.botToken = botToken;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.telegramUserService = telegramUserService;
        this.telegramChatService = telegramChatService;
        this.telegramMessageService = telegramMessageService;
    }

    private Map<String, Object> createMessageBody(String chatId, String message, Long replyToMessageId) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", message);
        body.put("disable_notification", "True");

        if (replyToMessageId != null) {
            body.put("reply_to_message_id", String.valueOf(replyToMessageId));
        }

        // Add link_preview_options with is_disabled = true
        Map<String, Object> linkPreviewOptions = new HashMap<>();
        linkPreviewOptions.put("is_disabled", true);
        body.put("link_preview_options", linkPreviewOptions);

        return body;
    }

    public void sendMessage(Long chatId, String message, Long replyToMessageId) {
        URI uri = UriComponentsBuilder.fromUriString(String.format(TELEGRAM_API_URL, botToken))
                .build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = createMessageBody(String.valueOf(chatId), message, replyToMessageId);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse the response and save the sent message
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                if (responseBody.path("ok").asBoolean(false)) {
                    JsonNode resultNode = responseBody.path("result");
                    Long sentMessageId = resultNode.path("message_id").asLong();
                    Integer date = resultNode.path("date").asInt();
                    String text = resultNode.path("text").asText();

                    // Handle bot user and chat information
                    JsonNode fromNode = resultNode.path("from");
                    JsonNode chatNode = resultNode.path("chat");
                    Long botUserId = telegramUserService.handleUserInformation(fromNode);
                    Long savedChatId = telegramChatService.handleChatInformation(chatNode);

                    // Save the bot's message
                    // Pass null for replyToMessageId as this is the bot's own message, not a reply in the DB sense
                    telegramMessageService.saveMessage(sentMessageId, botUserId, savedChatId, date, text, null);
                } else {
                     System.err.println("Error sending message: Telegram API returned ok=false. Response: " + response.getBody());
                }
            } else {
                System.err.println("Error sending message: Telegram API returned status " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            System.err.println("Telegram API error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Catch Jackson parsing errors as well
            System.err.println("Error sending message or processing response: " + ex.getMessage());
            ex.printStackTrace(); // Print stack trace for detailed debugging
        }
    }
}
