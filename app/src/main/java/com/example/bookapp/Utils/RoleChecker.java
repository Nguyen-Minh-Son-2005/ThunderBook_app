package com.example.bookapp.Utils;

import android.app.Activity;
import android.widget.Toast;

import com.example.bookapp.Model.User;

/**
 * Gọi ở onCreate() của mọi Activity trong package View/admin để chặn
 * user thường (role != "admin") truy cập trái phép, kể cả khi họ cố tình
 * mở màn admin bằng deep link hoặc back-stack.
 *
 * Dùng kèm với Firestore Security Rules ở tầng backend - đây chỉ là lớp
 * chặn UI, không thay thế được rules.
 */
public class RoleChecker {

    public interface OnPermissionResult {
        void onGranted();
    }

    public static void checkAdminPermission(Activity activity, OnPermissionResult onGranted) {
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid == null) {
            activity.finish();
            return;
        }

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null && user.isAdmin()) {
                        onGranted.onGranted();
                    } else {
                        Toast.makeText(activity, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                })
                .addOnFailureListener(e -> activity.finish());
    }


    /** Interface MỚI — dùng riêng cho 2 hàm bổ sung bên dưới, không ảnh hưởng OnPermissionResult cũ. */
    public interface OnRoleResult {
        void onResult(boolean isAdmin);
    }

    /**
     * Kiểm tra NHANH quyền admin dựa vào SharedPreferences cục bộ (SessionManager — file riêng
     * của Admin), không cần chờ mạng. Dùng để quyết định điều hướng ngay khi mở app, KHÔNG
     * dùng để cấp quyền ghi dữ liệu (dữ liệu cục bộ có thể bị chỉnh sửa trên máy đã root).
     */
    public static boolean isAdminLocal(Activity activity) {
        return new SessionManager(activity).isAdmin();
    }

    /**
     * Kiểm tra quyền admin CHẮC CHẮN từ Firestore nhưng KHÔNG tự Toast/finish Activity —
     * chỉ trả về true/false qua callback. Dùng khi chỉ cần biết kết quả để hiện/ẩn 1 phần UI
     * (vd switch "Cấp quyền admin" trong màn chi tiết user), không phải để chặn cả màn hình.
     * Nếu cần chặn cả màn hình như trước giờ, tiếp tục dùng checkAdminPermission() đã có.
     */
    public static void checkRoleSilently(Activity activity, OnRoleResult callback) {
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid == null) {
            callback.onResult(false);
            return;
        }
        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onResult(false);
                        return;
                    }
                    try {
                        User user = doc.toObject(User.class);
                        if (user == null) {
                            callback.onResult(false);
                        } else {
                            // Lưu session nội bộ để các màn hình khác (như Dashboard) có thể lấy tên/avatar
                            new SessionManager(activity).saveSession(
                                    user.getUserId(),
                                    user.getRole(),
                                    user.getFullName(),
                                    user.getAvatarUrl()
                            );

                            if (!user.isAdmin()) {
                                callback.onResult(false);
                            } else {
                                callback.onResult(true);
                            }
                        }
                    } catch (Exception ex) {
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onResult(false);
                });
    }

}
