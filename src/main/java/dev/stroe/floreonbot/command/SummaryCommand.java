package dev.stroe.floreonbot.command;

import java.util.List;

import org.springframework.stereotype.Component;

import dev.stroe.floreonbot.entity.TelegramMessage;
import dev.stroe.floreonbot.repository.TelegramMessageRepository;
import dev.stroe.floreonbot.service.ChatGPTService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class SummaryCommand implements Command {
    private final ChatGPTService chatGPT;
    private final TelegramSendMessageService telegramSendMessage;
    private final TelegramMessageRepository telegramMessageRepository;

    public SummaryCommand(ChatGPTService chatGPT, TelegramSendMessageService telegramSendMessage,
            TelegramMessageRepository telegramMessageRepository) {
        this.chatGPT = chatGPT;
        this.telegramSendMessage = telegramSendMessage;
        this.telegramMessageRepository = telegramMessageRepository;
    }

    @Override
    public void execute(String text, Long chatId, Long userId, Long messageId) {
        try {
            int hours = Integer.parseInt(text);
            
            // Check if requested hours exceed the maximum limit
            if (hours > 12) {
                telegramSendMessage.sendMessage(chatId, "Maximum allowed time period is 12 hours. Please try again with a smaller number.", messageId);
                return;
            }
            
            long now = System.currentTimeMillis() / 1000L;
            long xHoursAgo = now - (hours * 3600L); // where x is the number of hours

            List<TelegramMessage> messages = telegramMessageRepository.findByChatIdAndDateBetween(chatId, xHoursAgo,
                    now);

            if (messages.isEmpty()) {
                telegramSendMessage.sendMessage(chatId, "No messages found in the specified time period.", messageId);
                return;
            }

            // Collect all messages in a single string
            StringBuilder conversationText = new StringBuilder();
            for (TelegramMessage message : messages) {
                String fullName = message.getFrom().getFullName() != null ? message.getFrom().getFullName()
                        : "Unknown User";
                conversationText.append(fullName).append(": ").append(message.getText()).append("\n");
            }

            // Send to ChatGPT for summarization
            String prompt = "Please summarize in Romanian language the following conversation:\n"
                    + conversationText.toString();

            String summary = chatGPT.chatGPTResponse(prompt, false);

            // Send the summary back to the user
            telegramSendMessage.sendMessage(chatId, summary, messageId);
        } catch (NumberFormatException e) {
            telegramSendMessage.sendMessage(chatId,
                    "Please provide a valid number of hours after the /summary command!", messageId);
        } catch (Exception e) {
            System.err.println("Error generating summary: " + e.getMessage());
            telegramSendMessage.sendMessage(chatId, "Error generating summary. Please try again later.", messageId);
        }
    }

    @Override
    public String getDescription() {
        return "Get a summary of the conversation from last X hours";
    }
}