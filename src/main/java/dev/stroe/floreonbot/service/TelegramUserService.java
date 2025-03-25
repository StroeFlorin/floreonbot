package dev.stroe.floreonbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import dev.stroe.floreonbot.entity.TelegramUser;
import dev.stroe.floreonbot.repository.TelegramUserRepository;

@Service
public class TelegramUserService {
    
    private final TelegramUserRepository userRepository;
    
    public TelegramUserService(TelegramUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public Long handleUserInformation(JsonNode fromNode) {
        Long userId = fromNode.path("id").asLong();
        Boolean isBot = fromNode.path("is_bot").asBoolean();
        String firstName = fromNode.path("first_name").asText("");
        String lastName = fromNode.path("last_name").asText("");
        String username = fromNode.path("username").asText("");

        TelegramUser user = userRepository.findById(userId).orElse(new TelegramUser());
        user.setId(userId);
        user.setIsBot(isBot);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        userRepository.save(user);
        
        return userId;
    }
}