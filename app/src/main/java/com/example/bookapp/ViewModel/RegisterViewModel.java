package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.User;
import com.example.bookapp.Repository.UserRepository;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel cho RegisterActivity.
 * Xử lý toàn bộ luồng đăng ký: tạo Auth account → lưu document User vào Firestore.
 */
public class RegisterViewModel extends ViewModel {

    private final UserRepository userRepository = new UserRepository();

    private final MutableLiveData<Boolean> _registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getRegisterSuccess() { return _registerSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    /**
     * Tạo tài khoản Firebase Auth, sau đó lưu thông tin User vào Firestore.
     * role mặc định luôn là "user" khi tự đăng ký qua app.
     */
    public void register(String fullName, String email, String phone, String password) {
        FirebaseUtils.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        _errorMessage.setValue("Không lấy được thông tin tài khoản vừa tạo");
                        return;
                    }

                    User newUser = new User(
                            firebaseUser.getUid(),
                            fullName,
                            email,
                            phone,
                            "",                    // avatarUrl để trống, user sửa sau ở EditProfile
                            Constants.ROLE_USER,
                            "male",                // giới tính mặc định
                            null                   // birthday null
                    );

                    userRepository.createUser(newUser, new com.example.bookapp.Utils.FirebaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            _registerSuccess.setValue(true);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            _errorMessage.setValue("Lỗi lưu thông tin: " + e.getMessage());
                        }
                    });
                })
                .addOnFailureListener(e -> _errorMessage.setValue("Đăng ký thất bại: " + e.getMessage()));
    }
}
