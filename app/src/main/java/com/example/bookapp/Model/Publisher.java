package com.example.bookapp.Model;

/**
 * Model tương ứng bảng "publishers"
 */
public class Publisher {

    @com.google.firebase.firestore.DocumentId
    private String publisherId;
    private String name;
    private String logoUrl;
    private String description;

    public Publisher() {
    }

    public Publisher(String publisherId, String name, String logoUrl, String description) {
        this.publisherId = publisherId;
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
