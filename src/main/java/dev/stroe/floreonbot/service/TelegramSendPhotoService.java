package dev.stroe.floreonbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class TelegramSendPhotoService {

    private static final Logger log = LoggerFactory.getLogger(TelegramSendPhotoService.class);
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendPhoto";

    private final RestTemplate restTemplate;
    private final String botToken;
    private final ObjectMapper objectMapper;
    private final TelegramSendMessageService telegramSendMessageService; // For sending text messages (e.g., errors)
    private final TelegramUserService telegramUserService;
    private final TelegramChatService telegramChatService;
    private final TelegramMessageService telegramMessageService;


    public TelegramSendPhotoService(
            @Value("${telegram.bot.token}") String botToken,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            TelegramSendMessageService telegramSendMessageService,
            TelegramUserService telegramUserService,
            TelegramChatService telegramChatService,
            TelegramMessageService telegramMessageService) {
        this.botToken = botToken;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.telegramSendMessageService = telegramSendMessageService;
        this.telegramUserService = telegramUserService;
        this.telegramChatService = telegramChatService;
        this.telegramMessageService = telegramMessageService;
    }

    public void sendPhoto(Long chatId, byte[] photoBytes, String caption, Long replyToMessageId) {
        URI uri = UriComponentsBuilder.fromUriString(String.format(TELEGRAM_API_URL, botToken))
                .build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("chat_id", String.valueOf(chatId));

        // Create a ByteArrayResource with a filename (required for multipart)
        ByteArrayResource photoResource = new ByteArrayResource(photoBytes) {
            @Override
            public String getFilename() {
                return "photo.jpg"; // Or generate a more specific filename if needed
            }
        };
        body.add("photo", photoResource);

        if (caption != null && !caption.isEmpty()) {
            body.add("caption", caption);
        }
        if (replyToMessageId != null) {
            body.add("reply_to_message_id", String.valueOf(replyToMessageId));
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                if (responseBody.path("ok").asBoolean(false)) {
                    log.info("Successfully sent photo to chat {}", chatId);
                    JsonNode resultNode = responseBody.path("result");
                    Long sentMessageId = resultNode.path("message_id").asLong();
                    Integer date = resultNode.path("date").asInt();
                    // Caption might be empty, handle appropriately if needed for saving
                    String savedCaption = resultNode.path("caption").asText(null);

                    JsonNode fromNode = resultNode.path("from");
                    JsonNode chatNode = resultNode.path("chat");
                    Long botUserId = telegramUserService.handleUserInformation(fromNode);
                    Long savedChatId = telegramChatService.handleChatInformation(chatNode);

                    // Save the bot's message (photo with caption)
                    // Pass null for replyToMessageId in DB context
                    telegramMessageService.saveMessage(sentMessageId, botUserId, savedChatId, date, savedCaption, null);

                } else {
                    log.error("Failed to send photo to chat {}: Telegram API returned ok=false. Response: {}", chatId, response.getBody());
                    telegramSendMessageService.sendMessage(chatId, "Sorry, I couldn't send the image.", replyToMessageId);
                }
            } else {
                log.error("Failed to send photo to chat {}: Telegram API returned status {}", chatId, response.getStatusCode());
                telegramSendMessageService.sendMessage(chatId, "Sorry, I couldn't send the image.", replyToMessageId);
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Telegram API error while sending photo to chat {}: {} - {}", chatId, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            telegramSendMessageService.sendMessage(chatId, "Sorry, I couldn't send the image due to an API error.", replyToMessageId);
        } catch (Exception e) {
            log.error("Exception occurred while sending photo to chat {}: {}", chatId, e.getMessage(), e);
            telegramSendMessageService.sendMessage(chatId, "An unexpected error occurred while trying to send the image.", replyToMessageId);
        }
    }
}
