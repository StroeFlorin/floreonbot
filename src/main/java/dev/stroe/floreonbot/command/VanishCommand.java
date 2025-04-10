package dev.stroe.floreonbot.command;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import dev.stroe.floreonbot.entity.TelegramMessage;
import dev.stroe.floreonbot.repository.TelegramMessageRepository;
import dev.stroe.floreonbot.service.TelegramDeleteMessageService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class VanishCommand implements Command {
    private final TelegramSendMessageService telegramSendMessage;
    private final TelegramMessageRepository telegramMessageRepository;
    private final TelegramDeleteMessageService telegramDeleteMessageService;

    public VanishCommand(TelegramSendMessageService telegramSendMessage,
            TelegramMessageRepository telegramMessageRepository,
            TelegramDeleteMessageService telegramDeleteMessageService) {
        this.telegramSendMessage = telegramSendMessage;
        this.telegramMessageRepository = telegramMessageRepository;
        this.telegramDeleteMessageService = telegramDeleteMessageService;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        try {
            int numberOfMessages = Integer.parseInt(text);
            numberOfMessages++;
            if (numberOfMessages <= 1) {
                telegramSendMessage.sendMessage(chatId, "Please provide a number greater than 0.", messageId);
                return;
            }
            if(numberOfMessages > 10) {
                telegramSendMessage.sendMessage(chatId, "Please provide a number less than 10.", messageId);
                return;
            }

            List<TelegramMessage> messages = telegramMessageRepository.findLatestMessagesByChatIdAndUserId(chatId,
                    userId, numberOfMessages);

            if (messages.isEmpty()) {
                telegramSendMessage.sendMessage(chatId, "No messages found.", messageId);
                return;
            }

            List<Long> messageIds = messages.stream()
                    .map(TelegramMessage::getMessageId)
                    .collect(Collectors.toList());
            telegramDeleteMessageService.deleteMessages(chatId, messageIds);

            for (TelegramMessage message : messages) {
                telegramMessageRepository.deleteById(message.getMessageId());
            }

            telegramSendMessage.sendMessage(chatId, "Deleted " + (messageIds.size()) + " messages from " + messages.get(0).getFrom().getFullName() + "!", null);

        } catch (NumberFormatException e) {
            telegramSendMessage.sendMessage(chatId, "Please provide a valid number.", messageId);
            return;
        }
    }

    @Override
    public String getDescription() {
        return "Deletes x messages from the chat.";
    }

}
