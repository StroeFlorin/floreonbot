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

    public DailySummarySchedulerService(TelegramSendMessageService telegramSendMessageService,
            TelegramMessageRepository telegramMessageRepository, TelegramChatRepository telegramChatRepository) {
        this.telegramChatRepository = telegramChatRepository;
        this.telegramSendMessageService = telegramSendMessageService;
        this.telegramMessageRepository = telegramMessageRepository;
    }

    @Scheduled(cron = "0 59 23 * * *")
    public void sendDailySummary() {
        List<TelegramChat> chats = telegramChatRepository.findAll();
        for (TelegramChat chat : chats) {
            Long currentChatId = chat.getId();
                TelegramUser user = telegramMessageRepository.findMostActiveChatterByChatId(currentChatId, 
                        (int) (System.currentTimeMillis() / 1000) - 86400,
                        (int) (System.currentTimeMillis() / 1000));
                if (user != null) {
                    String message = String.format("🥇 %s was the most active chatter today, posting %d messages! ",
                            user.getFullName(), getMessageCountOfUserToday(currentChatId, user.getId()));
                    telegramSendMessageService.sendMessage(currentChatId, message, null);
                }
        }
    }

    public long getMessageCountOfUserToday(Long chatId, Long userId) {
        LocalDate today = LocalDate.now();
        long todayStart = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long todayEnd = today.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    
        return telegramMessageRepository.countMessagesByChatIdAndUserIdToday(
            chatId, userId, todayStart, todayEnd);
    }
}