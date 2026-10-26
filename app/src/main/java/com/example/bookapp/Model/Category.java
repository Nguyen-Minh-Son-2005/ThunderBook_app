package com.example.bookapp.Model;

/**
 * Model tương ứng bảng "categories"
 */
public class Category {

    @com.google.firebase.firestore.DocumentId
    private String categoryId;
    private String name;
    private String imageUrl;
    private boolean isActive;

    public Category() {
    }

    public Category(String categoryId, String name, String imageUrl, boolean isActive) {
        this.categoryId = categoryId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @com.google.firebase.firestore.PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @com.google.firebase.firestore.PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }
}
