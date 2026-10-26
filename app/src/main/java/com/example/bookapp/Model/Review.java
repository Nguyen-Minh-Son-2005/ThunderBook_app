package com.example.bookapp.Model;

import com.google.firebase.Timestamp;

/**
 * Model tương ứng bảng "reviews"
 */
public class Review {

    private String reviewId;
    private String bookId;
    private String userId;
    private String userName;   // snapshot tên user lúc đánh giá
    private String userAvatar; // snapshot avatar user lúc đánh giá
    private double rating;     // 1 - 5
    private String comment;
    private String orderId;    // chỉ cho review nếu đã mua (verified purchase)
    private Timestamp createdAt;

    public Review() {
    }

    public Review(String reviewId, String bookId, String userId, String userName,
                  String userAvatar, double rating, String comment, String orderId,
                  Timestamp createdAt) {
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.rating = rating;
        this.comment = comment;
        this.orderId = orderId;
        this.createdAt = createdAt;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isVerifiedPurchase() {
        return orderId != null && !orderId.isEmpty();
    }
}
