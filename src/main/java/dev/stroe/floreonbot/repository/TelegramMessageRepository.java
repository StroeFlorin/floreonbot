package dev.stroe.floreonbot.repository;

import dev.stroe.floreonbot.entity.TelegramMessage;
import dev.stroe.floreonbot.entity.TelegramUser;

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

    @Query(value = "SELECT u.* FROM telegram_user u " +
                   "JOIN (SELECT user_id, COUNT(*) AS message_count " +
                   "      FROM telegram_message " +
                   "      WHERE chat_id = :chatId " +
                   "      AND date >= :todayStart AND date < :todayEnd " +
                   "      GROUP BY user_id " +
                   "      ORDER BY message_count DESC " +
                   "      LIMIT 1) AS most_active " +
                   "ON u.id = most_active.user_id", nativeQuery = true)
    TelegramUser findMostActiveChatterByChatId(@Param("chatId") Long chatId, 
                                              @Param("todayStart") Integer todayStart, 
                                              @Param("todayEnd") Integer todayEnd);

    @Query(value = "SELECT COUNT(*) FROM telegram_message WHERE chat_id = :chatId AND user_id = :userId AND date >= :todayStart AND date < :todayEnd", nativeQuery = true)
    long countMessagesByChatIdAndUserIdToday(@Param("chatId") Long chatId, @Param("userId") Long userId, @Param("todayStart") Long todayStart, @Param("todayEnd") Long todayEnd);
}