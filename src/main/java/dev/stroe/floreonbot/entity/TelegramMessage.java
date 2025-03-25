package dev.stroe.floreonbot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "telegram_message")
public class TelegramMessage {
    @Id
    private Long messageId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private TelegramUser from;

    private Integer date;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "chat_id")
    private TelegramChat chat;

    @ManyToOne
    @JoinColumn(name = "reply_to_message_id")
    private TelegramMessage replyToMessage;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    // Getters and setters
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public TelegramUser getFrom() {
        return from;
    }

    public void setFrom(TelegramUser from) {
        this.from = from;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public TelegramChat getChat() {
        return chat;
    }

    public void setChat(TelegramChat chat) {
        this.chat = chat;
    }

    public TelegramMessage getReplyToMessage() {
        return replyToMessage;
    }

    public void setReplyToMessage(TelegramMessage replyToMessage) {
        this.replyToMessage = replyToMessage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}