package dev.stroe.floreonbot.repository;

import dev.stroe.floreonbot.entity.TelegramChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TelegramChatRepository extends JpaRepository<TelegramChat, Long> {
    
    List<TelegramChat> findAll();
}