package dev.stroe.floreonbot.repository;

import dev.stroe.floreonbot.entity.TelegramMessage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, Long> {
    List<TelegramMessage> findByChatIdAndDateBetween(Long chatId, Long startDate, Long endDate);
}