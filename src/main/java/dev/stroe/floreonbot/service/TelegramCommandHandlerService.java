package dev.stroe.floreonbot.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import dev.stroe.floreonbot.command.*;

@Service
public class TelegramCommandHandlerService {


    private final TelegramSendMessageService telegramSendMessage;
    private final TelegramSendChatAction telegramSendChatAction;
    private final Map<String, Command> commandRegistry = new HashMap<>();

    public Map<String, Command> getCommandRegistry() {
        return commandRegistry;
    }

    public TelegramCommandHandlerService(
            TelegramSendMessageService telegramSendMessage,
            AskCommand askCommand,
            GreetCommand greetCommand,
            HelpCommand helpCommand,
            SummaryCommand summaryCommand,
            AskWebCommand askWebCommand,
            TelegramSendChatAction telegramSendChatAction,
            WeatherCommand weatherCommand) {
        this.telegramSendMessage = telegramSendMessage;
        this.telegramSendChatAction = telegramSendChatAction;

        registerCommand("ask", askCommand);
        registerCommand("askweb", askWebCommand);
        registerCommand("hello", greetCommand);
        registerCommand("help", helpCommand);
        helpCommand.setCommandRegistry(commandRegistry);
        registerCommand("summary", summaryCommand);
        registerCommand("weather", weatherCommand);
    }

    private void registerCommand(String commandName, Command command) {
        commandRegistry.put(commandName, command);
    }

    public void handleCommands(String text, Long chatId, Long userId, Long messageId) {
        String userMessage = text.trim();

        if (!userMessage.startsWith("/")) {
            return; // Not a command
        }

        // Extract command name and arguments
        String[] parts = userMessage.substring(1).split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        // Execute command if it exists
        Command command = commandRegistry.get(commandName);
        if (command != null) {
            telegramSendChatAction.sendTypingAction(chatId);
            command.execute(args, chatId, userId, messageId);
        } else {
            // Handle unknown command
            telegramSendMessage.sendMessage(chatId, "Unknown command: " + commandName, messageId);
        }
    }
}