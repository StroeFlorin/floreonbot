package dev.stroe.floreonbot.command;

import java.util.List;

import org.springframework.stereotype.Component;

import dev.stroe.floreonbot.entity.TelegramMessage;
import dev.stroe.floreonbot.repository.TelegramMessageRepository;
import dev.stroe.floreonbot.service.GeminiService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class GeminiCommand implements Command {
    private final GeminiService geminiService;
    private final TelegramSendMessageService telegramSendMessage;
    private final TelegramMessageRepository telegramMessageRepository;

    public GeminiCommand(GeminiService geminiService, TelegramSendMessageService telegramSendMessage,
            TelegramMessageRepository telegramMessageRepository) {
        this.geminiService = geminiService;
        this.telegramSendMessage = telegramSendMessage;
        this.telegramMessageRepository = telegramMessageRepository;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        if (!text.isEmpty()) {

            // Get the last 20 messages from the chat
            List<TelegramMessage> lastMessages = telegramMessageRepository.findLatestMessagesByChatId(chatId, 20);

            // Build context string from messages
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("Chat context (last 20 messages):\n");

            for (TelegramMessage message : lastMessages) {
                contextBuilder.append(message.getFrom().getFullName()).append(": ").append(message.getText())
                        .append("\n");
            }

            contextBuilder.append("\nNow answer this question: ").append(text);

            String messageToBeSentToGemini = contextBuilder.toString();

            String response = geminiService.ask(messageToBeSentToGemini);
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