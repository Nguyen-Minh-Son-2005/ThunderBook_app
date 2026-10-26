package com.example.bookapp.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.User;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;

import java.util.Map;

/**
 * Lớp DUY NHẤT được phép gọi Firestore cho collection "users".
 * ViewModel gọi qua đây, không được tự gọi FirebaseUtils.getFirestore() nữa.
 */
public class UserRepository {

    /**
     * Tải thông tin 1 user theo uid. Trả về LiveData để ViewModel observe -
     * đây là fetch 1 lần (không realtime), set giá trị vào LiveData khi có kết quả.
     */
    public LiveData<User> getUser(String uid) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        user.setUserId(doc.getId());
                    }
                    liveData.setValue(user);
                })
                .addOnFailureListener(e -> liveData.setValue(null));

        return liveData;
    }

    /** Tạo document user mới - dùng khi đăng ký tài khoản (RegisterActivity). */
    public void createUser(@NonNull User user, FirebaseCallback<Void> callback) {
        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_USERS)
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /** Cập nhật 1 phần thông tin user (vd sửa hồ sơ) - dùng Map để chỉ update field cần thiết. */
    public void updateUser(String uid, Map<String, Object> updates, FirebaseCallback<Void> callback) {
        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_USERS)
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}
