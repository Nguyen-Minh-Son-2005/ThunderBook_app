package com.example.bookapp.Adapter.admin;

/** DTO thuần UI cho lưới 3 cột "Quản lý cửa hàng" trên Dashboard — không phải Model Firestore. */
public class AdminDashboardMenuItem {
    private final int iconResId;
    private final String label;
    /** Định danh để Activity biết bấm vào ô nào — dùng String thay vì Class<?> để tránh
     * import chéo tới từng Activity ngay trong DTO, giữ package Adapter gọn nhẹ. */
    private final String targetKey;

    public AdminDashboardMenuItem(int iconResId, String label, String targetKey) {
        this.iconResId = iconResId;
        this.label = label;
        this.targetKey = targetKey;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getLabel() {
        return label;
    }

    public String getTargetKey() {
        return targetKey;
    }
}
