package dev.stroe.floreonbot.command;

import java.util.List;

import org.springframework.stereotype.Component;

import dev.stroe.floreonbot.entity.TelegramMessage;
import dev.stroe.floreonbot.repository.TelegramMessageRepository;
import dev.stroe.floreonbot.service.ChatGPTService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class ChatGPTWebCommand implements Command {
    private final ChatGPTService chatGPT;
    private final TelegramSendMessageService telegramSendMessage;
    private final TelegramMessageRepository telegramMessageRepository;

    public ChatGPTWebCommand(ChatGPTService chatGPT, TelegramSendMessageService telegramSendMessage,
            TelegramMessageRepository telegramMessageRepository) {
        this.chatGPT = chatGPT;
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
                contextBuilder.append(message.getFrom().getFullName()).append(": ")
                        .append(message.getText()).append("\n");
            }

            contextBuilder.append("\nNow answer this question: ").append(text);

            String messageToBeSentToChatGPT = contextBuilder.toString();

            String gptResponse = chatGPT.chatGPTResponse(messageToBeSentToChatGPT, true);
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