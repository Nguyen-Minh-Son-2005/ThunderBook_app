package com.example.bookapp.Adapter.admin;

/**
 * DTO thuần túy để hiển thị UI, KHÔNG phải Model Firestore — dữ liệu tổng hợp (đếm/sum)
 * do ViewModel tự tính từ nhiều Repository rồi đóng gói vào đây cho Adapter hiển thị.
 * Đặt trong package Adapter (không phải Model) vì nó không tương ứng với 1 document
 * Firestore nào cả.
 */
public class AdminDashboardStat {
    private int iconResId;
    private String value;
    private String label;
    private String growthLabel; // vd "▲ 12.5%", để trống nếu không cần hiện

    public AdminDashboardStat(int iconResId, String value, String label, String growthLabel) {
        this.iconResId = iconResId;
        this.value = value;
        this.label = label;
        this.growthLabel = growthLabel;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getGrowthLabel() {
        return growthLabel;
    }
}
