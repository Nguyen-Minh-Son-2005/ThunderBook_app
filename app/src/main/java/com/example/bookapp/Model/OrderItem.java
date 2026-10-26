package com.example.bookapp.Model;

/**
 * Model đại diện cho 1 phần tử trong field "items" (List) của bảng "orders".
 * Đây là snapshot thông tin sách tại thời điểm đặt hàng, KHÔNG lấy trực tiếp
 * từ bảng "books" để tránh lịch sử đơn hàng bị sai lệch khi admin đổi giá/ảnh
 * hoặc xóa sách sau này.
 */
public class OrderItem {

    private String bookId;
    private String title;
    private String coverImageUrl;
    private double price;    // giá tại thời điểm mua
    private int quantity;

    public OrderItem() {
    }

    public OrderItem(String bookId, String title, String coverImageUrl,
                      double price, int quantity) {
        this.bookId = bookId;
        this.title = title;
        this.coverImageUrl = coverImageUrl;
        this.price = price;
        this.quantity = quantity;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getLineTotal() {
        return price * quantity;
    }
}
