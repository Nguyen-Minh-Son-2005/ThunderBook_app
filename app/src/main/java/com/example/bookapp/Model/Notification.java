package com.example.bookapp.Model;

import com.google.firebase.Timestamp;

/**
 * Model tương ứng bảng "notifications"
 */
public class Notification {

    private String notificationId;
    private String userId;   // null nếu gửi broadcast toàn user
    private String title;
    private String content;
    private String type;     // "order" | "promo" | "system"
    private String relatedId; // id của đối tượng liên quan (ví dụ: orderId)
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {
    }

    public Notification(String notificationId, String userId, String title, String content,
                         String type, String relatedId, boolean isRead, Timestamp createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.relatedId = relatedId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }
}
