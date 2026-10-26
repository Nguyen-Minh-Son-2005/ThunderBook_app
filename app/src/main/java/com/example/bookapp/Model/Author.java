package com.example.bookapp.Model;

/**
 * Model tương ứng bảng "authors"
 */
public class Author {

    @com.google.firebase.firestore.DocumentId
    private String authorId;
    private String name;
    private String avatarUrl;
    private String bio;
    private int bookCount;

    public Author() {
    }

    public Author(String authorId, String name, String avatarUrl, String bio, int bookCount) {
        this.authorId = authorId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.bookCount = bookCount;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getBookCount() {
        return bookCount;
    }

    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }
}
