package dev.stroe.floreonbot.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import dev.stroe.floreonbot.exception.CurrencyConversionException;
import dev.stroe.floreonbot.service.CurrencyConverterService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class CurrencyConverterCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyConverterCommand.class);
    private final CurrencyConverterService currencyConverterService;
    private final TelegramSendMessageService telegramSendMessageService;

    public CurrencyConverterCommand(CurrencyConverterService currencyConverterService,
            TelegramSendMessageService telegramSendMessageService) {
        this.currencyConverterService = currencyConverterService;
        this.telegramSendMessageService = telegramSendMessageService;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        String[] parts = text.trim().split("\\s+");
        if (parts.length != 4 || !parts[2].equalsIgnoreCase("TO")) {
            telegramSendMessageService.sendMessage(chatId,
            "Invalid format. Usage: /convert 10 RON TO USD", messageId);
            return;
        }

        try {
            double amount = Double.parseDouble(parts[0]);
            String fromCurrency = parts[1].toUpperCase();
            String toCurrency = parts[3].toUpperCase();

            logger.info("Converting {} {} to {}", amount, fromCurrency, toCurrency);
            double conversionRate = currencyConverterService.convertCurrency(fromCurrency, toCurrency, amount);
            String responseMessage = String.format("%s %s = %s %s", 
                    formatNumber(amount), fromCurrency,
                    formatNumber(conversionRate), toCurrency);
  
            telegramSendMessageService.sendMessage(chatId, responseMessage, messageId);

        } catch (NumberFormatException e) {
            logger.warn("Invalid amount format: {}", parts[0], e);
            telegramSendMessageService.sendMessage(chatId, "Invalid amount format. Please enter a valid number.",
                    messageId);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid argument: {}", e.getMessage());
            telegramSendMessageService.sendMessage(chatId,
                    "Invalid currency conversion request: " + e.getMessage(), messageId);
        } catch (CurrencyConversionException e) {
            logger.error("Currency conversion error: {}", e.getMessage(), e);
            String userMessage = getUserFriendlyErrorMessage(e);
            telegramSendMessageService.sendMessage(chatId, userMessage, messageId);
        } catch (Exception e) {
            logger.error("Unexpected error in currency conversion command", e);
            telegramSendMessageService.sendMessage(chatId,
                    "An unexpected error occurred. Please try again later.", messageId);
        }
    }
    
    private String getUserFriendlyErrorMessage(CurrencyConversionException e) {
        switch (e.getErrorType()) {
            case "unsupported-code":
                return "Currency code not supported. Please check that both currencies are valid.";
            case "malformed-request":
                return "Invalid request format. Please use the format: /convert 10 RON TO USD";
            case "invalid-key":
                return "Service authentication error. Please contact the bot administrator.";
            case "inactive-account":
            case "quota-reached":
                return "Currency conversion service limit reached. Please try again later.";
            case "network_error":
            case "server_error":
                return "Currency conversion service is temporarily unavailable. Please try again later.";
            default:
                return "Error converting currency: " + e.getMessage();
        }
    }

    /**
     * Formats a double value to display as an integer if it has no decimal part,
     * or with decimal places if it does.
     */
    private String formatNumber(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
        }
    }

    @Override
    public String getDescription() {
        return "Convert currencies. Usage: /convert 10 RON to USD";
    }
}
