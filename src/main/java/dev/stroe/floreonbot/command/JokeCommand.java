package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;
import dev.stroe.floreonbot.service.GeminiService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class JokeCommand implements Command {
    private final TelegramSendMessageService telegramSendMessage;
    private final GeminiService geminiService;

    public JokeCommand(TelegramSendMessageService telegramSendMessage, GeminiService geminiService) {
        this.telegramSendMessage = telegramSendMessage;
        this.geminiService = geminiService;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        String response;
        if(text == null || text.isEmpty()) {
            response = geminiService.ask("Tell me a joke.");
        } else {
            response = geminiService.ask("Tell me a joke about " + text);
        }
        telegramSendMessage.sendMessage(chatId, response, messageId);
    }

    @Override
    public String getDescription() {
        return "The bot tells you a joke. Usage: /joke [topic]";
    }
}
