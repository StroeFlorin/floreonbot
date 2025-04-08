package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

import dev.stroe.floreonbot.service.TelegramSendPollService;

@Component
public class SendPollCommand implements Command{
    private final TelegramSendPollService telegramSendPollService;


    public SendPollCommand(TelegramSendPollService telegramSendPollService) {
        this.telegramSendPollService = telegramSendPollService;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        String[] parts = text.split(",");
        String question = parts[0].trim();
        List<String> options = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            options.add(parts[i].trim());
        }
        telegramSendPollService.sendPoll(chatId, question, options);
    }

    @Override
    public String getDescription() {
        return "Send a poll to the chat. Usage: /poll question, option1, option2,...";
    }

}
