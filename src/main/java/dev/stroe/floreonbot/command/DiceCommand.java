package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;
import dev.stroe.floreonbot.service.ChatGPTService;
import dev.stroe.floreonbot.service.TelegramSendDiceService;

@Component
public class DiceCommand implements Command {
    private final TelegramSendDiceService telegramSendDiceService;

    public DiceCommand(ChatGPTService chatGPT, TelegramSendDiceService telegramSendDiceService) {
        this.telegramSendDiceService = telegramSendDiceService;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        telegramSendDiceService.sendDice(chatId);
    }

    @Override
    public String getDescription() {
        return "Roll a dice.";
    }
}