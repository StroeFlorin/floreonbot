package dev.stroe.floreonbot.command;


import dev.stroe.floreonbot.service.GeminiImageGenerationService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;
import dev.stroe.floreonbot.service.TelegramSendPhotoService; // Import SendPhotoService
import dev.stroe.floreonbot.service.GeminiImageGenerationService.ImageGenerationException; // Import custom exception
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class GenerateImageCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(GenerateImageCommand.class);

    private final GeminiImageGenerationService geminiImageGeneration;
    private final TelegramSendMessageService telegramSendMessageService;
    private final TelegramSendPhotoService telegramSendPhotoService; // Added SendPhotoService

    // Updated constructor
    public GenerateImageCommand(
            GeminiImageGenerationService geminiImageGeneration,
            TelegramSendMessageService telegramSendMessageService,
            TelegramSendPhotoService telegramSendPhotoService) { // Added SendPhotoService
        this.geminiImageGeneration = geminiImageGeneration;
        this.telegramSendMessageService = telegramSendMessageService;
        this.telegramSendPhotoService = telegramSendPhotoService; // Assign SendPhotoService
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        if (text == null || text.trim().isEmpty()) {
            telegramSendMessageService.sendMessage(chatId, "Please provide a prompt after the command!", messageId);
            return;
        }

        String prompt = text.trim();
        telegramSendMessageService.sendMessage(chatId, "Generating image for: " + prompt + "...", messageId);

        try {
            byte[] imageBytes = geminiImageGeneration.generateImage(prompt);

            // Send the photo using the dedicated service
            telegramSendPhotoService.sendPhoto(chatId, imageBytes, prompt, messageId);
            log.info("Successfully generated and sent image for chat {}", chatId);

        } catch (ImageGenerationException e) {
            // Handle specific image generation errors from the service
            log.error("Image generation failed for chat {}: {}", chatId, e.getMessage());
            telegramSendMessageService.sendMessage(chatId, e.getMessage(), messageId); // Send the specific error message
        } catch (Exception e) {
            // Handle other unexpected errors during the process
            log.error("Unexpected error during image generation command for chat {}: {}", chatId, e.getMessage(), e);
            telegramSendMessageService.sendMessage(chatId, "An unexpected error occurred while trying to generate the image.", messageId);
        }
    }

    @Override
    public String getDescription() {
        return "Generates an image based on a text prompt using Gemini. Usage: /generateimage [prompt]";
    }
}
