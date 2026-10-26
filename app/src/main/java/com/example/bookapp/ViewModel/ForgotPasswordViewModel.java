package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Utils.FirebaseUtils;

/**
 * ViewModel cho ForgotPasswordActivity.
 * Gọi trực tiếp FirebaseAuth (không cần Repository riêng vì
 * chỉ reset password, không đọc/ghi Firestore).
 */
public class ForgotPasswordViewModel extends ViewModel {

    private final MutableLiveData<Boolean> _resetEmailSent = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getResetEmailSent() { return _resetEmailSent; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    /** Gửi email đặt lại mật khẩu tới địa chỉ email được cung cấp. */
    public void sendResetEmail(String email) {
        FirebaseUtils.getAuth().sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> _resetEmailSent.setValue(true))
                .addOnFailureListener(e -> _errorMessage.setValue(e.getMessage()));
    }
}
