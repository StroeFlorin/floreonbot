package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;
import dev.stroe.floreonbot.service.ChatGPTService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class JokeCommand implements Command {
    private final TelegramSendMessageService telegramSendMessage;
    private final ChatGPTService chatGPTService;

    public JokeCommand(TelegramSendMessageService telegramSendMessage, ChatGPTService chatGPTService) {
        this.telegramSendMessage = telegramSendMessage;
        this.chatGPTService = chatGPTService;
    }

    @Override
    public void execute(String text, Long chatId, Long userId, Long messageId) {
        String gptResponse="";
        if(text == null || text.isEmpty()) {
            gptResponse = chatGPTService.chatGPTResponse("Tell me a joke in romanian.", false);
        } else {
            gptResponse = chatGPTService.chatGPTResponse("Spune-mi o gluma/banc despre " + text, false);
        }
            telegramSendMessage.sendMessage(chatId, gptResponse, messageId);
    }

    @Override
    public String getDescription() {
        return "The bot tells you a joke. Usage: /joke [topic]";
    }
}
