package dev.stroe.floreonbot.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.stroe.floreonbot.entity.TelegramMessage;
import dev.stroe.floreonbot.repository.TelegramMessageRepository;

@Service
public class ChatGPTRandomInteractionService {
    //private final GeminiService geminiService;
    private final ChatGPTService chatGPTService;
    private final TelegramSendMessageService telegramSendMessage;
    private final TelegramMessageRepository telegramMessageRepository;

    public ChatGPTRandomInteractionService(TelegramSendMessageService telegramSendMessage,
            TelegramMessageRepository telegramMessageRepository, ChatGPTService chatGPTService) {

        this.telegramSendMessage = telegramSendMessage;
        this.telegramMessageRepository = telegramMessageRepository;
        this.chatGPTService = chatGPTService;
    }

    // Implement the logic for random interactions with ChatGPT here
    public void randomInteraction(Long chatId) {
        // Get the last 20 messages from the chat
        List<TelegramMessage> lastMessages = telegramMessageRepository.findLatestMessagesByChatId(chatId, 20);

        // Reverse the order of messages to be oldest to newest
        Collections.reverse(lastMessages);

        // Build context string from messages
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("You are a friendly, natural, and helpful telegram bot acting like a person chatting on Telegram. Your name is Floreon_bot. You remember the last 20 messages of the conversation and use that context to reply in a human-like way—responding casually, humorously, or seriously depending on the tone. Always keep replies concise unless the user asks for more. Feel free to ask questions back, joke around, or give thoughtful advice—whatever fits the vibe. Your goal is to make the user feel like they’re chatting with a real person, not a bot. Your answer should be in the language the context is. Your answer should be informal. The messages are sorted from the oldest to the newest. Here is the chat context (last 20 messages):\n");

        for (TelegramMessage message : lastMessages) {
            contextBuilder.append(message.getFrom().getFullName()).append(": ")
                    .append(message.getText()).append("\n");
        }

        contextBuilder.append("Your response should be like this: <your message> without mentioning your name!");

        String messageToBeSentToChatGPT = contextBuilder.toString();

        String chatGPTResponse = chatGPTService.chatGPTResponse(messageToBeSentToChatGPT, false);
        telegramSendMessage.sendMessage(chatId, chatGPTResponse, null);
    }
}
