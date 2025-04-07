package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;
import dev.stroe.floreonbot.service.ChatGPTService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class ChatGPTWebCommand implements Command {
    private final ChatGPTService chatGPT;
    private final TelegramSendMessageService telegramSendMessage;

    public ChatGPTWebCommand(ChatGPTService chatGPT, TelegramSendMessageService telegramSendMessage) {
        this.chatGPT = chatGPT;
        this.telegramSendMessage = telegramSendMessage;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        if (!text.isEmpty()) {
            String gptResponse = chatGPT.chatGPTResponse(text, true);
            telegramSendMessage.sendMessage(chatId, gptResponse, messageId);
        } else {
            telegramSendMessage.sendMessage(chatId, "Please provide a question after /askweb command!", messageId);
        }
    }

    @Override
    public String getDescription() {
        return "Ask ChatGPT anything. It will search the internet.";
    }
}