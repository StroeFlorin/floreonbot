package dev.stroe.floreonbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.stroe.floreonbot.entity.ChatInteractionStatus;

@Repository
public interface ChatInteractionStatusRepository extends JpaRepository<ChatInteractionStatus, Long> {
}