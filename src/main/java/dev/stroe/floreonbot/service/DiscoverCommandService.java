package dev.stroe.floreonbot.service;

import org.springframework.stereotype.Service;

@Service
public class DiscoverCommandService {
    ChatGPTService chatGPTService;

    public DiscoverCommandService(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    public String discoverCommand(String messageReceived) {
        String message= "You are a telegram bot. Your job is to recognize commands in chat. For example:\n" + //
                        "\"Tell me the weather in Buzau\" should recognize command /weather Buzau.\n" + //
                        "The list of commands are:\n" + //
                        "/summary - Get a summary of the conversation from last X hours. Usage /summary x\n" + //
                        "/dice - Roll a dice.\n" + //
                        "/meteo - Get the current weather for a location. Usage /meteo Location\n" + //
                        "/forecast - Get a 7-day weather forecast for a location. Usage /forecast Location\n" + //
                        "/poll - Send a poll to the chat. Usage: /poll question, option1, option2,...\n" + //
                        "/convert - Convert currencies. Usage: /convert 10 EUR to USD\n" + //
                        "/joke - The bot tells you a joke. Usage: /joke topic\n" + //
                        "/gemini - Ask Google's Gemini AI a question. Usage /gemini question\n" + //
                        "/help - Show available commands.\n" + //
                        "/temperature - Get the current temperature for a location. Usage /temperature Location\n" + //
                        "/weather - Get the current weather for a location. Usage /weather Location\n" + //
                        "/hello - Get a greeting from the bot.\n" + //
                        "/togglechat - Toggle chat interactions on or off. Optionally, provide a percentage (0-100) to set the interaction level.\n" + //
                        "/chatgpt - Ask ChatGPT a question. Usage /chatgpt question\n" + //
                        "/chatgptweb - Ask ChatGPT anything. It will search the internet. Usage /chatgptweb question\n" + //
                        "/vanish - Deletes n number of messages from the chat. Usage /vanish n\n" + //
                        "\n" + //
                        "The bot will respond with the command it recognized. If no command is recognized, it will say \"No command found\".\n" + //
                        "The message is: " + messageReceived;
        return chatGPTService.chatGPTResponse(message, false);
    }
}
