package com.example.bookapp.Model;

import java.util.List;

/**
 * Model tương ứng bảng "books"
 */
@com.google.firebase.firestore.IgnoreExtraProperties
public class Book {

    @com.google.firebase.firestore.DocumentId
    private String bookId;
    private String title;
    private String slug;
    private List<String> authorIds;
    private String publisherId;
    private List<String> categoryIds;
    private String description;
    private String coverImageUrl;
    private double price;
    private double salePrice;
    private int stock;
    private int soldCount;
    private int viewCount;
    private double rating;
    private int ratingCount;
    private int publishYear;
    private List<String> tags;
    private boolean isFeatured;
    private boolean isActive;

    @com.google.firebase.firestore.Exclude
    private String authorNameDisplay = "";

    public Book() {
    }

    public Book(String bookId, String title, String slug, List<String> authorIds,
                String publisherId, List<String> categoryIds, String description,
                String coverImageUrl, double price, double salePrice, int stock,
                int soldCount, int viewCount, double rating, int ratingCount,
                int publishYear, List<String> tags, boolean isFeatured, boolean isActive) {
        this.bookId = bookId;
        this.title = title;
        this.slug = slug;
        this.authorIds = authorIds;
        this.publisherId = publisherId;
        this.categoryIds = categoryIds;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.price = price;
        this.salePrice = salePrice;
        this.stock = stock;
        this.soldCount = soldCount;
        this.viewCount = viewCount;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.publishYear = publishYear;
        this.tags = tags;
        this.isFeatured = isFeatured;
        this.isActive = isActive;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public List<String> getAuthorIds() {
        return authorIds;
    }

    public void setAuthorIds(List<String> authorIds) {
        this.authorIds = authorIds;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @com.google.firebase.firestore.PropertyName("isFeatured")
    public boolean isFeatured() {
        return isFeatured;
    }

    @com.google.firebase.firestore.PropertyName("isFeatured")
    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    @com.google.firebase.firestore.PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @com.google.firebase.firestore.PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * True nếu salePrice > 0 và nhỏ hơn giá gốc -> đang khuyến mãi.
     */
    @com.google.firebase.firestore.Exclude
    public boolean isOnSale() {
        return salePrice > 0 && salePrice < price;
    }

    @com.google.firebase.firestore.Exclude
    public String getAuthorNameDisplay() {
        return authorNameDisplay;
    }

    @com.google.firebase.firestore.Exclude
    public void setAuthorNameDisplay(String authorNameDisplay) {
        this.authorNameDisplay = authorNameDisplay;
    }
}
