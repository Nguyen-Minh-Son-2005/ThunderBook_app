package com.example.bookapp.Repository;

import android.net.Uri;

import com.example.bookapp.Utils.FirebaseCallback;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Lớp DUY NHẤT được phép gọi Firebase Storage.
 * Tách ra khỏi UserRepository vì upload ảnh không thuộc về
 * collection "users" thuần túy trên Firestore.
 * Dùng bởi EditProfileViewModel.
 */
public class StorageRepository {

    private static final String AVATARS_PATH = "avatars/";

    /**
     * Upload ảnh đại diện lên Firebase Storage.
     * Sau khi upload xong, trả về downloadUrl qua callback để
     * EditProfileViewModel cập nhật tiếp vào Firestore.
     *
     * @param uid       uid của user đang đăng nhập (dùng làm tên file)
     * @param imageUri  Uri ảnh được chọn từ gallery
     * @param callback  onSuccess(downloadUrl), onFailure(exception)
     */
    public void uploadAvatar(String uid, Uri imageUri, FirebaseCallback<String> callback) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference(AVATARS_PATH + uid + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(downloadUri ->
                                        callback.onSuccess(downloadUri.toString()))
                                .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);
    }
}
