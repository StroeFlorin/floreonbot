package dev.stroe.floreonbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import dev.stroe.floreonbot.entity.TelegramChat;
import dev.stroe.floreonbot.repository.TelegramChatRepository;

@Service
public class TelegramChatService {
    
    private final TelegramChatRepository chatRepository;
    
    public TelegramChatService(TelegramChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }
    
    public Long handleChatInformation(JsonNode chatNode) {
        Long chatId = chatNode.path("id").asLong();
        String chatType = chatNode.path("type").asText("");
        String chatTitle = chatNode.path("title").asText("");

        TelegramChat chat = chatRepository.findById(chatId).orElse(new TelegramChat());
        chat.setId(chatId);
        chat.setType(chatType);
        chat.setTitle(chatTitle);
        chatRepository.save(chat);

        return chatId;
    }
}