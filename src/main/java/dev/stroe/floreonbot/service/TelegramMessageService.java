package dev.stroe.floreonbot.service;

import org.springframework.stereotype.Service;
import dev.stroe.floreonbot.entity.TelegramMessage;
import dev.stroe.floreonbot.repository.TelegramMessageRepository;
import dev.stroe.floreonbot.repository.TelegramUserRepository;
import dev.stroe.floreonbot.repository.TelegramChatRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class TelegramMessageService {
    
    private final TelegramMessageRepository messageRepository;
    private final TelegramUserRepository userRepository;
    private final TelegramChatRepository chatRepository;
    
    public TelegramMessageService(
            TelegramMessageRepository messageRepository,
            TelegramUserRepository userRepository,
            TelegramChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
    }
    
    public void saveMessage(
            Long messageId, 
            Long userId, 
            Long chatId, 
            Integer date, 
            String text, 
            Long replyToMessageId) {
        
        TelegramMessage message = new TelegramMessage();
        message.setMessageId(messageId);
        message.setFrom(userRepository.findById(userId).orElse(null));
        message.setChat(chatRepository.findById(chatId).orElse(null));
        message.setDate(date);
        message.setText(text);
        
        if (replyToMessageId != null) {
            message.setReplyToMessage(messageRepository.findById(replyToMessageId).orElse(null));
        }
        
        messageRepository.save(message);
    }
}