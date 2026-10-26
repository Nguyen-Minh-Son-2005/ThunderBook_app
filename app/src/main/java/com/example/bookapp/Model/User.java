package com.example.bookapp.Model;

import com.google.firebase.Timestamp;

/**
 * Model tương ứng bảng "users"
 * Dùng chung cho cả Admin và User, phân quyền qua field role.
 */
public class User {

    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String role;      // "admin" | "user"
    private String gender;
    private Timestamp birthday;

    // Bắt buộc: constructor rỗng để Firestore deserialize
    public User() {
    }

    public User(String userId, String fullName, String email, String phone,
                String avatarUrl, String role, String gender, Timestamp birthday) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.gender = gender;
        this.birthday = birthday;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Timestamp getBirthday() {
        return birthday;
    }

    public void setBirthday(Timestamp birthday) {
        this.birthday = birthday;
    }

    @com.google.firebase.firestore.Exclude
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}
