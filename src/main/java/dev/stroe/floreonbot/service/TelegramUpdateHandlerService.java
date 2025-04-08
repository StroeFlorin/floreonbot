package dev.stroe.floreonbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class TelegramUpdateHandlerService {
    
    private final TelegramUserService userService;
    private final TelegramChatService chatService;
    private final TelegramMessageService messageService;
    private final TelegramCommandHandlerService commandHandler;
    
    public TelegramUpdateHandlerService(
            TelegramUserService userService,
            TelegramChatService chatService, 
            TelegramMessageService messageService,
            TelegramCommandHandlerService commandHandler,
            GeminiService geminiService) {
        this.userService = userService;
        this.chatService = chatService;
        this.messageService = messageService;
        this.commandHandler = commandHandler;
    }
    
    public void processUpdate(JsonNode update) {
        JsonNode messageNode = update.path("message");
        if (!messageNode.isMissingNode()) {
            processMessage(messageNode);
        }
        // Add other types of updates if needed (edited_message, callback_query, etc.)
    }
    
    private void processMessage(JsonNode messageNode) {
        Long messageId = messageNode.path("message_id").asLong();
        Integer date = messageNode.path("date").asInt();

        // Determine text content from either caption (if photo exists) or text field
        String text = "";
        
        // Check if message has photo
        JsonNode photoNode = messageNode.path("photo");
        if (!photoNode.isMissingNode() && photoNode.isArray() && photoNode.size() > 0) {
            // Photo exists, use caption if available
            text = messageNode.path("caption").asText("");
        } else {
            // No photo, use regular text field
            text = messageNode.path("text").asText("");
        }
        
        JsonNode fromNode = messageNode.path("from");
        if (fromNode.isMissingNode()) return;
        
        // Process user
        Long userId = userService.handleUserInformation(fromNode);
        
        JsonNode chatNode = messageNode.path("chat");
        if (chatNode.isMissingNode()) return;
        
        // Process chat
        Long chatId = chatService.handleChatInformation(chatNode);
        
        // Get reply info if present
        Long replyToMessageId = null;
        JsonNode replyNode = messageNode.path("reply_to_message");
        if (!replyNode.isMissingNode()) {
            replyToMessageId = replyNode.path("message_id").asLong();
        }
        
        // Save message
        messageService.saveMessage(messageId, userId, chatId, date, text, replyToMessageId);
        commandHandler.handleCommands(text, chatId, userId, messageId);
    }
}