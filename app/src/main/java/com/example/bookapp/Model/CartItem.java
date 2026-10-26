package com.example.bookapp.Model;

import com.google.firebase.Timestamp;

/**
 * Model tương ứng subcollection "carts/{userId}/items/{bookId}"
 */
public class CartItem {

    private String bookId;
    private int quantity;
    private double priceAtAdd; // lưu giá lúc thêm, tránh lệch khi giá sách đổi
    private Timestamp addedAt;

    public CartItem() {
    }

    public CartItem(String bookId, int quantity, double priceAtAdd, Timestamp addedAt) {
        this.bookId = bookId;
        this.quantity = quantity;
        this.priceAtAdd = priceAtAdd;
        this.addedAt = addedAt;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPriceAtAdd() {
        return priceAtAdd;
    }

    public void setPriceAtAdd(double priceAtAdd) {
        this.priceAtAdd = priceAtAdd;
    }

    public Timestamp getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Timestamp addedAt) {
        this.addedAt = addedAt;
    }

    public double getSubTotal() {
        return priceAtAdd * quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return bookId != null ? bookId.equals(cartItem.bookId) : cartItem.bookId == null;
    }

    @Override
    public int hashCode() {
        return bookId != null ? bookId.hashCode() : 0;
    }
}
