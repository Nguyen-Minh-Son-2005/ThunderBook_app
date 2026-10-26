package com.example.bookapp.Utils;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Tránh gọi lặp FirebaseFirestore.getInstance() / FirebaseAuth.getInstance()
 * ở mọi Activity/Repository - gọi qua đây cho gọn và dễ thay đổi sau này
 * (vd bật offline persistence, đổi region...).
 */
public class FirebaseUtils {

    private static FirebaseFirestore firestoreInstance;

    public static FirebaseFirestore getFirestore() {
        if (firestoreInstance == null) {
            firestoreInstance = FirebaseFirestore.getInstance();
        }
        return firestoreInstance;
    }

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    @Nullable
    public static String getCurrentUserId() {
        FirebaseUser user = getAuth().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static boolean isLoggedIn() {
        return getAuth().getCurrentUser() != null;
    }



    // --- Thêm 2 dòng field này ngay dưới "private static FirebaseFirestore firestoreInstance;" ---
// private static com.google.firebase.storage.FirebaseStorage storageInstance;

    /**
     * Cần cho ImageUtils.java (file riêng của Admin) để upload ảnh bìa sách/avatar lên
     * Firebase Storage. User hiện chưa cần Storage nên bản gốc chưa có — bổ sung thêm ở đây.
     */
    public static com.google.firebase.storage.FirebaseStorage getStorage() {
        return com.google.firebase.storage.FirebaseStorage.getInstance();
    }

    public static com.google.firebase.storage.StorageReference getStorageRef() {
        return getStorage().getReference();
    }

    /** Trả về FirebaseUser đầy đủ (khác getCurrentUserId() chỉ trả UID) — dự phòng cho sau này. */
    @Nullable
    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    /** Dùng cho nút "Đăng xuất" (fl_logout) trên AdminDashboardActivity. */
    public static void signOut() {
        getAuth().signOut();
    }

}
