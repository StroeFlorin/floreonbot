package dev.stroe.floreonbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "chat_interaction_status")
public class ChatInteractionStatus {
    @Id
    @Column(name = "telegram_chat")
    private Long telegramChat;

    @Column(name = "status")
    private boolean status;

    @Column(name = "percentage")
    private Integer percentage;

    public ChatInteractionStatus() {}

    public ChatInteractionStatus(Long telegramChat, boolean status) {
        this.telegramChat = telegramChat;
        this.status = status;
    }

    public Long getTelegramChat() { return telegramChat; }

    public void setTelegramChat(Long telegramChat) { this.telegramChat = telegramChat; }

    public boolean isStatus() { return status; }

    public void setStatus(boolean status) { this.status = status; }

    public Integer getPercentage() { return percentage; }

    public void setPercentage(Integer percentage) { this.percentage = percentage; }
}

