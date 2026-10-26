package com.example.bookapp.Adapter.admin;

/**
 * DTO thuần UI cho dòng xếp hạng trong StatisticActivity — dùng chung được cho CẢ "top
 * sách bán chạy" lẫn "top khách hàng mua nhiều nhất" vì cả 2 đều chỉ cần: ảnh đại diện,
 * tên, 1 con số phụ. Tránh viết 2 Adapter gần như giống hệt nhau chỉ khác Model nguồn.
 * ViewModel chịu trách nhiệm map Book/User thành AdminRankItem trước khi đưa cho Adapter.
 */
public class AdminRankItem {
    private final String avatarUrl;
    private final String name;
    private final String valueLabel; // vd "128 đã bán" hoặc "14 đơn hàng"

    public AdminRankItem(String avatarUrl, String name, String valueLabel) {
        this.avatarUrl = avatarUrl;
        this.name = name;
        this.valueLabel = valueLabel;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getName() {
        return name;
    }

    public String getValueLabel() {
        return valueLabel;
    }
}
