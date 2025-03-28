package dev.stroe.floreonbot.repository;

import dev.stroe.floreonbot.entity.TelegramMessage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, Long> {
    List<TelegramMessage> findByChatIdAndDateBetween(Long chatId, Long startDate, Long endDate);
    
    @Query(value = "SELECT * FROM telegram_message WHERE chat_id = :chatId AND user_id = :userId ORDER BY message_id DESC LIMIT :limit", nativeQuery = true)
    List<TelegramMessage> findLatestMessagesByChatIdAndUserId(@Param("chatId") Long chatId, @Param("userId") Long userId, @Param("limit") int limit);
}