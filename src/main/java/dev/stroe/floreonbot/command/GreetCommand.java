package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;
import dev.stroe.floreonbot.repository.TelegramMessageRepository;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class GreetCommand implements Command {
    private final TelegramSendMessageService telegramSendMessage;
    private final TelegramMessageRepository messageRepository;

    public GreetCommand(TelegramSendMessageService telegramSendMessage, TelegramMessageRepository messageRepository) {
        this.telegramSendMessage = telegramSendMessage;
        this.messageRepository = messageRepository;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        String fullName = messageRepository.findById(messageId).get().getFrom().getFullName();
        telegramSendMessage.sendMessage(chatId, "Hello, " + fullName + "!", messageId);
    }

    @Override
    public String getDescription() {
        return "Get a greeting from the bot.";
    }
}