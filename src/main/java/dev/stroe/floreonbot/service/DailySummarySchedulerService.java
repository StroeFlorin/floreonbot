package dev.stroe.floreonbot.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dev.stroe.floreonbot.entity.TelegramChat;
import dev.stroe.floreonbot.entity.TelegramUser;
import dev.stroe.floreonbot.repository.TelegramChatRepository;
import dev.stroe.floreonbot.repository.TelegramMessageRepository;

@Service
public class DailySummarySchedulerService {
    private final TelegramSendMessageService telegramSendMessageService;
    private final TelegramChatRepository telegramChatRepository;
    private final TelegramMessageRepository telegramMessageRepository;
    private LocalDate today;
    private long todayStart;
    private long todayEnd;

    public DailySummarySchedulerService(TelegramSendMessageService telegramSendMessageService,
            TelegramMessageRepository telegramMessageRepository, TelegramChatRepository telegramChatRepository) {
        this.telegramChatRepository = telegramChatRepository;
        this.telegramSendMessageService = telegramSendMessageService;
        this.telegramMessageRepository = telegramMessageRepository;
    }

    @Scheduled(cron = "0 30 13 * * *")
    public void sendDailySummary() {
        List<TelegramChat> chats = telegramChatRepository.findAll();
        today = LocalDate.now();
        todayStart = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        todayEnd = today.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        
        for (TelegramChat chat : chats) {
            Long currentChatId = chat.getId();
                List<TelegramUser> topChatters = telegramMessageRepository.findTopMostActiveChattersByChatId(
                        currentChatId, 
                        todayStart,
                        todayEnd);
                
                if (topChatters != null && !topChatters.isEmpty()) {
                    StringBuilder message = new StringBuilder("Today's most active chatters:\n\n");
                    
                    // Add appropriate emoji and rank for each chatter
                    for (int i = 0; i < topChatters.size(); i++) {
                        TelegramUser user = topChatters.get(i);
                        String emoji = i == 0 ? "🥇" : (i == 1 ? "🥈" : "🥉");
                        message.append(String.format("%s %s: %d messages (%d words)\n", 
                                emoji, 
                                user.getFullName(),
                                getMessageCountOfUserToday(currentChatId, user.getId()),
                                getWordCountOfUserToday(currentChatId, user.getId())));
                    }
                    
                    telegramSendMessageService.sendMessage(currentChatId, message.toString(), null);
                }
        }
    }

    public long getMessageCountOfUserToday(Long chatId, Long userId) {
        return telegramMessageRepository.countMessagesByChatIdAndUserIdToday(
            chatId, userId, todayStart, todayEnd);
    }
    
    public long getWordCountOfUserToday(Long chatId, Long userId) {
        List<String> messageTexts = telegramMessageRepository.findMessageTextsByChatIdAndUserIdToday(
            chatId, userId, todayStart, todayEnd);
        
        long totalWords = 0;
        for (String text : messageTexts) {
            if (text != null && !text.trim().isEmpty()) {
                // Split on whitespace and count the resulting array length
                String[] words = text.trim().split("\\s+");
                totalWords += words.length;
            }
        }
        
        return totalWords;
    }
}