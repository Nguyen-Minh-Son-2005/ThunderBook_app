package com.example.bookapp.Model;

import com.google.firebase.Timestamp;

import java.util.List;

/**
 * Model tương ứng bảng "orders"
 */
public class Order {

    private String orderId;
    private String userId;
    private List<OrderItem> items;
    private double totalPrice;
    private double shippingFee;
    private double discountAmount;
    private String voucherCode;
    private double finalTotal;
    private String paymentStatus;  // "pending" | "paid" | "failed"
    private String orderStatus;    // "pending" | "confirmed" | "packing" | "shipping" | "delivered" | "cancelled" | "returned"
    private Address shippingAddress; // snapshot địa chỉ lúc đặt hàng
    private String note;
    private Timestamp createdAt;

    public Order() {
    }

    public Order(String orderId, String userId, List<OrderItem> items, double totalPrice,
                 double shippingFee, double discountAmount, String voucherCode,
                 double finalTotal, String paymentStatus, String orderStatus,
                 Address shippingAddress, String note, Timestamp createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.shippingFee = shippingFee;
        this.discountAmount = discountAmount;
        this.voucherCode = voucherCode;
        this.finalTotal = finalTotal;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.shippingAddress = shippingAddress;
        this.note = note;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public double getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(double finalTotal) {
        this.finalTotal = finalTotal;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Tổng số lượng sách trong đơn (dùng hiển thị "3 sản phẩm" trên UI).
     */
    public int getTotalItemCount() {
        int count = 0;
        if (items != null) {
            for (OrderItem item : items) {
                count += item.getQuantity();
            }
        }
        return count;
    }
}
