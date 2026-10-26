package com.example.bookapp.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.User;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminUserRepository {

    private final CollectionReference usersRef =
            FirebaseUtils.getFirestore().collection(Constants.COLLECTION_USERS);

    /** Realtime toàn bộ user — dùng cho ManageUserActivity. */
    public LiveData<List<User>> observeAllUsers() {
        return new FirestoreListLiveData<>(usersRef, User.class);
    }

    /** Lấy 1 lần — dùng cho màn chi tiết user (đổi quyền, xem lịch sử mua). */
    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> result = new MutableLiveData<>();
        usersRef.document(userId).get()
                .addOnSuccessListener(doc -> result.setValue(doc.toObject(User.class)));
        return result;
    }

    /**
     * Đổi quyền admin/user cho 1 tài khoản — chỉ cập nhật field role, không ghi đè cả
     * document. Đây là thao tác NHẠY CẢM, nên ViewModel/Activity gọi hàm này cần có dialog
     * xác nhận trước khi gọi.
     */
    public void updateUserRole(String userId, String newRole, FirebaseCallback<Void> callback) {
        usersRef.document(userId).update(Constants.FIELD_ROLE, newRole)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /** Lọc theo role — dùng cho 3 chip "Tất cả/Khách hàng/Quản trị viên" trong ManageUserActivity. */
    public List<User> filterByRole(List<User> source, String role) {
        if (role == null || role.isEmpty()) return source;
        List<User> result = new ArrayList<>();
        for (User u : source) {
            if (role.equalsIgnoreCase(u.getRole())) result.add(u);
        }
        return result;
    }

    /** Tìm theo tên hoặc email. */
    public List<User> filterByNameOrEmail(List<User> source, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return source;
        List<User> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT).trim();
        for (User u : source) {
            boolean matchName = u.getFullName() != null
                    && u.getFullName().toLowerCase(Locale.ROOT).contains(lowerKeyword);
            boolean matchEmail = u.getEmail() != null
                    && u.getEmail().toLowerCase(Locale.ROOT).contains(lowerKeyword);
            if (matchName || matchEmail) result.add(u);
        }
        return result;
    }

    /** Đếm nhanh tổng số user — dùng cho thẻ thống kê "Người dùng" trên Dashboard. */
    public LiveData<Integer> countAllUsers() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        usersRef.get().addOnSuccessListener(snapshots -> result.setValue(snapshots.size()));
        return result;
    }
}
