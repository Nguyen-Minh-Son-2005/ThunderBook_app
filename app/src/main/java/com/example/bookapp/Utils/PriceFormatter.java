package com.example.bookapp.Utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Format giá tiền VNĐ và tính % giảm giá dùng chung nhiều màn: item_admin_book,
 * BookDetailActivity, CheckoutActivity, item_admin_order... Tránh mỗi nơi tự viết
 * NumberFormat/SimpleDateFormat khác nhau dẫn đến hiển thị không nhất quán.
 */
public class PriceFormatter {

    private static final Locale VN_LOCALE = new Locale("vi", "VN");

    private PriceFormatter() {
    }

    /** VD: 79000 -> "79.000đ" */
    public static String formatVND(double price) {
        NumberFormat formatter = NumberFormat.getInstance(VN_LOCALE);
        return formatter.format(price) + "đ";
    }

    public static String formatVND(long price) {
        return formatVND((double) price);
    }

    /** Tính % giảm giá từ giá gốc và giá sale, làm tròn xuống. VD: 99000, 79000 -> 20 */
    public static int calculateDiscountPercent(double originalPrice, double salePrice) {
        if (originalPrice <= 0 || salePrice <= 0 || salePrice >= originalPrice) return 0;
        return (int) Math.floor((1 - (salePrice / originalPrice)) * 100);
    }

    /** true nếu sách đang có giá sale thấp hơn giá gốc — dùng để quyết định hiện badge giảm giá. */
    public static boolean isOnSale(double originalPrice, double salePrice) {
        return salePrice > 0 && salePrice < originalPrice;
    }

    /** VD: "12/08/2026 - 14:32" — dùng cho tv_order_date, item_timeline_step... */
    public static String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", VN_LOCALE);
        return sdf.format(date);
    }

    /** VD: "12/08/2026" — dùng cho HSD voucher, ngày sinh... */
    public static String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", VN_LOCALE);
        return sdf.format(date);
    }

    /**
     * Overload nhận thẳng com.google.firebase.Timestamp — Firestore SDK trả về kiểu này
     * nếu Model khai field là Timestamp thay vì java.util.Date (tùy cách bạn viết Model).
     * Có overload này thì Adapter/ViewModel gọi PriceFormatter.formatDateTime(xxx) hay
     * formatDate(xxx) đều tự compile đúng, không cần nhớ gọi .toDate() thủ công ở từng chỗ.
     */
    public static String formatDateTime(com.google.firebase.Timestamp timestamp) {
        return timestamp == null ? "" : formatDateTime(timestamp.toDate());
    }

    public static String formatDate(com.google.firebase.Timestamp timestamp) {
        return timestamp == null ? "" : formatDate(timestamp.toDate());
    }
}
