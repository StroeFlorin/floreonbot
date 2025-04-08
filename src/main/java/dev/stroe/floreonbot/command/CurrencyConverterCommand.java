package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;

import dev.stroe.floreonbot.service.CurrencyConverterService;
import dev.stroe.floreonbot.service.TelegramSendMessageService;

@Component
public class CurrencyConverterCommand implements Command {
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

            double conversionRate = currencyConverterService.convertCurrency(fromCurrency, toCurrency, amount);
            String responseMessage = String.format("%.2f %s = %.2f %s", amount, fromCurrency,
                    conversionRate, toCurrency);
  
            telegramSendMessageService.sendMessage(chatId, responseMessage, messageId);

        } catch (NumberFormatException e) {
            telegramSendMessageService.sendMessage(chatId, "Invalid amount format. Please enter a valid number.",
                    messageId);
        } catch (Exception e) {
            telegramSendMessageService.sendMessage(chatId,
                    "Error converting currency. Please check the currency codes.", messageId);
        }
    }

    @Override
    public String getDescription() {
        return "Convert currencies. Usage: /convert 10 RON TO USD";
    }
}
