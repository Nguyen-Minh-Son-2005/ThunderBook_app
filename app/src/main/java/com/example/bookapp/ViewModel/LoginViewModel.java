package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Utils.FirebaseUtils;

/**
 * ViewModel cho LoginActivity.
 * Giữ trạng thái đăng nhập qua xoay màn hình, tránh gọi lại Firebase Auth không cần thiết.
 */
public class LoginViewModel extends ViewModel {

    private final MutableLiveData<Boolean> _loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getLoginSuccess() { return _loginSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    /**
     * Gọi Firebase Auth signInWithEmailAndPassword.
     * Kết quả được post vào LiveData để Activity observe và cập nhật UI.
     */
    public void login(String email, String password) {
        FirebaseUtils.getAuth().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> _loginSuccess.setValue(true))
                .addOnFailureListener(e -> _errorMessage.setValue(e.getMessage()));
    }

    /**
     * Gọi Firebase Auth signInWithCredential cho đăng nhập Google.
     */
    public void loginWithGoogle(com.google.firebase.auth.AuthCredential credential) {
        FirebaseUtils.getAuth().signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    com.google.firebase.auth.FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        FirebaseUtils.getFirestore().collection(com.example.bookapp.Utils.Constants.COLLECTION_USERS)
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (!documentSnapshot.exists()) {
                                        String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
                                        String name = firebaseUser.getDisplayName();
                                        if (name == null || name.trim().isEmpty()) {
                                            name = "User";
                                        }
                                        String avatar = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "";
                                        
                                        com.example.bookapp.Model.User newUser = new com.example.bookapp.Model.User(
                                                uid, name, email, "", avatar, "user", "", null
                                        );
                                        
                                        FirebaseUtils.getFirestore().collection(com.example.bookapp.Utils.Constants.COLLECTION_USERS)
                                                .document(uid)
                                                .set(newUser)
                                                .addOnSuccessListener(aVoid -> _loginSuccess.setValue(true))
                                                .addOnFailureListener(e -> _errorMessage.setValue("Lỗi khi lưu thông tin: " + e.getMessage()));
                                    } else {
                                        _loginSuccess.setValue(true);
                                    }
                                })
                                .addOnFailureListener(e -> _errorMessage.setValue("Lỗi kiểm tra người dùng: " + e.getMessage()));
                    } else {
                        _loginSuccess.setValue(true);
                    }
                })
                .addOnFailureListener(e -> _errorMessage.setValue(e.getMessage()));
    }
}
