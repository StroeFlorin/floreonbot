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
            response = geminiService.ask("Spune-mi o gluma.");
        } else {
            response = geminiService.ask("Spune-mi o gluma despre " + text);
        }
        telegramSendMessage.sendMessage(chatId, response, null);
    }

    @Override
    public String getDescription() {
        return "The bot tells you a joke. Usage: /joke [topic]";
    }
}
