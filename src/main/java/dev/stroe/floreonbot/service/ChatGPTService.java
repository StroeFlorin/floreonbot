package dev.stroe.floreonbot.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ChatGPTService {
    private static final int DEFAULT_MAX_COMPLETION_TOKENS = 2048;

    private final OpenAIClient client;

    public ChatGPTService(@Value("${openai.api.key}") String apiKey) {
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public String chatGPTResponse(String message, Boolean webSearch) {
        ChatModel model;
        if(webSearch) {
            model = ChatModel.GPT_4O_MINI_SEARCH_PREVIEW;
        } else {
            model = ChatModel.GPT_4_1_MINI;
        }
        
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(model)
                .maxCompletionTokens(DEFAULT_MAX_COMPLETION_TOKENS)
                .addUserMessage(message)
                .build();

        return client.chat().completions().create(params)
                .choices().stream()
                .flatMap(choice -> choice.message().content().stream())
                .collect(Collectors.joining("\n"));
    }
}