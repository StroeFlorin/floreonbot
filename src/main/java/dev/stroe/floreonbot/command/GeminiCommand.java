package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;
import dev.stroe.floreonbot.service.GeminiService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class GeminiCommand implements Command {
    private final GeminiService geminiService;
    private final TelegramSendMessageService telegramSendMessage;

    public GeminiCommand(GeminiService geminiService, TelegramSendMessageService telegramSendMessage) {
        this.geminiService = geminiService;
        this.telegramSendMessage = telegramSendMessage;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        if (!text.isEmpty()) {
            String response = geminiService.ask(text);
            telegramSendMessage.sendMessage(chatId, response, messageId);
        } else {
            telegramSendMessage.sendMessage(chatId, "Please provide a question after /gemini command!", messageId);
        }
    }

    @Override
    public String getDescription() {
        return "Ask Google's Gemini AI a question.";
    }
}