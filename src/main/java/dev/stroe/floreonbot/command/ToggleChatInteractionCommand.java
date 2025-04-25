package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;
import dev.stroe.floreonbot.entity.ChatInteractionStatus;
import dev.stroe.floreonbot.repository.ChatInteractionStatusRepository;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class ToggleChatInteractionCommand implements Command {
    private final ChatInteractionStatusRepository chatInteractionStatusRepository;
    private final TelegramSendMessageService telegramSendMessage;

    public ToggleChatInteractionCommand(ChatInteractionStatusRepository chatInteractionStatusRepository,
            TelegramSendMessageService telegramSendMessage) {
        this.chatInteractionStatusRepository = chatInteractionStatusRepository;
        this.telegramSendMessage = telegramSendMessage;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        String args = text.strip();
        // If user provided a percentage argument, update percentage
        if (!args.isEmpty()) {
            try {
                int pct = Integer.parseInt(args);
                if (pct < 0 || pct > 100) {
                    telegramSendMessage.sendMessage(chatId, "Please provide a percentage between 0 and 100.", messageId);
                    return;
                }
                ChatInteractionStatus status = chatInteractionStatusRepository.findById(chatId)
                        .orElseGet(() -> new ChatInteractionStatus(chatId, true));
                status.setPercentage(pct);
                chatInteractionStatusRepository.save(status);
                telegramSendMessage.sendMessage(chatId, "Interaction percentage set to " + pct + "%.", messageId);
                return;
            } catch (NumberFormatException e) {
                telegramSendMessage.sendMessage(chatId, "Please provide a valid integer percentage.", messageId);
                return;
            }
        }
        // No argument: toggle on/off
        var optStatus = chatInteractionStatusRepository.findById(chatId);
        boolean newStatus;
        ChatInteractionStatus status;
        if (optStatus.isPresent()) {
            status = optStatus.get();
            newStatus = !status.isStatus();
            status.setStatus(newStatus);
        } else {
            newStatus = false; // default enabled, so toggling disables
            status = new ChatInteractionStatus(chatId, newStatus);
        }
        chatInteractionStatusRepository.save(status);
        String response = newStatus ? "Chat interactions enabled." : "Chat interactions disabled.";
        telegramSendMessage.sendMessage(chatId, response, messageId);
    }

    @Override
    public String getDescription() {
        return "Toggle chat interactions on or off. Optionally, provide a percentage (0-100) to set the interaction level.";
    }
}
