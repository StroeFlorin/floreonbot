package dev.stroe.floreonbot.command;

public interface Command {
    void execute(String text, Long chatId, Long userId, Long messageId);
    String getDescription(); // For help functionality
}