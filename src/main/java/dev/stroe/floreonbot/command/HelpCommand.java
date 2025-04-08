package dev.stroe.floreonbot.command;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.boot.info.BuildProperties;

import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class HelpCommand implements Command {
    private final TelegramSendMessageService telegramSendMessage;
    private Map<String, Command> commandRegistry;
    private final BuildProperties buildProperties;

    public HelpCommand(TelegramSendMessageService telegramSendMessage, BuildProperties buildProperties) {
        this.telegramSendMessage = telegramSendMessage;
        this.buildProperties = buildProperties;
    }
    
    public void setCommandRegistry(Map<String, Command> commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        StringBuilder helpMessage = new StringBuilder("Available commands:\n");
        
        for (Map.Entry<String, Command> entry : commandRegistry.entrySet()) {
            helpMessage.append("/").append(entry.getKey())
                       .append(" - ").append(entry.getValue().getDescription())
                       .append("\n");
        }

        helpMessage.append("\nBot Version: ").append(buildProperties.getVersion());
        
        telegramSendMessage.sendMessage(chatId, helpMessage.toString(), messageId);
    }

    @Override
    public String getDescription() {
        return "Show available commands.";
    }
}