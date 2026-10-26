package com.example.bookapp.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model tin nhắn trong phiên chat giữa User và AI Chatbot.
 */
public class ChatMessage {

    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;

    private String messageId;
    private String text;
    private int senderType; // 0 = TYPE_USER, 1 = TYPE_BOT
    private long timestamp;
    private List<Book> suggestedBooks;
    private boolean isLoading;

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
        this.suggestedBooks = new ArrayList<>();
    }

    public ChatMessage(String text, int senderType) {
        this.text = text;
        this.senderType = senderType;
        this.timestamp = System.currentTimeMillis();
        this.suggestedBooks = new ArrayList<>();
        this.isLoading = false;
    }

    public ChatMessage(String text, int senderType, List<Book> suggestedBooks) {
        this.text = text;
        this.senderType = senderType;
        this.timestamp = System.currentTimeMillis();
        this.suggestedBooks = suggestedBooks != null ? suggestedBooks : new ArrayList<>();
        this.isLoading = false;
    }

    public static ChatMessage createLoadingMessage() {
        ChatMessage msg = new ChatMessage("", TYPE_BOT);
        msg.setLoading(true);
        return msg;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getSenderType() {
        return senderType;
    }

    public void setSenderType(int senderType) {
        this.senderType = senderType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Book> getSuggestedBooks() {
        return suggestedBooks;
    }

    public void setSuggestedBooks(List<Book> suggestedBooks) {
        this.suggestedBooks = suggestedBooks;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isUser() {
        return senderType == TYPE_USER;
    }

    public boolean hasSuggestedBooks() {
        return suggestedBooks != null && !suggestedBooks.isEmpty();
    }
}
