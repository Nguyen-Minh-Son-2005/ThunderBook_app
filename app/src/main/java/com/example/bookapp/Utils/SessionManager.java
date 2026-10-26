package com.example.bookapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lưu tạm thông tin đăng nhập (userId, role, tên, avatar) bằng SharedPreferences,
 * để không phải query lại Firestore mỗi lần mở app chỉ để biết role là admin hay user.
 *
 * LƯU Ý QUAN TRỌNG: SessionManager chỉ dùng để ĐIỀU HƯỚNG NHANH lúc mở app (UX mượt).
 * Với các thao tác NHẠY CẢM (vào màn Admin, ghi dữ liệu quan trọng), vẫn phải xác thực
 * lại role thật từ Firestore qua RoleChecker — vì dữ liệu SharedPreferences có thể bị
 * chỉnh sửa thủ công trên thiết bị đã root.
 */
public class SessionManager {

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /** Gọi ngay sau khi đăng nhập / đăng ký thành công và đã lấy được role từ Firestore. */
    public void saveSession(String userId, String role, String fullName, String avatarUrl) {
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USER_ROLE, role);
        editor.putString(Constants.KEY_USER_NAME, fullName);
        editor.putString(Constants.KEY_USER_AVATAR, avatarUrl);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, null);
    }

    public String getUserRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, Constants.ROLE_USER);
    }

    public String getUserName() {
        return prefs.getString(Constants.KEY_USER_NAME, "");
    }

    public String getUserAvatar() {
        return prefs.getString(Constants.KEY_USER_AVATAR, "");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    /** Kiểm tra nhanh — chỉ dùng để quyết định điều hướng, KHÔNG dùng để cấp quyền ghi dữ liệu. */
    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equalsIgnoreCase(getUserRole());
    }

    /** Gọi sau khi user cập nhật tên/avatar ở EditProfileActivity, không cần đăng nhập lại. */
    public void updateProfile(String fullName, String avatarUrl) {
        editor.putString(Constants.KEY_USER_NAME, fullName);
        editor.putString(Constants.KEY_USER_AVATAR, avatarUrl);
        editor.apply();
    }

    /** Gọi khi đăng xuất — xóa sạch phiên làm việc lưu cục bộ. */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
