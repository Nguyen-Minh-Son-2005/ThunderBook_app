package com.example.bookapp.ViewModel;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.User;
import com.example.bookapp.Repository.UserRepository;
import com.example.bookapp.Utils.FirebaseCallback;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel cho EditProfileActivity.
 * Tách load user hiện tại + update Firestore.
 */
public class EditProfileViewModel extends ViewModel {

    private final UserRepository userRepository = new UserRepository();

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<User> getUser() { return _user; }
    public LiveData<Boolean> getSaveSuccess() { return _saveSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    public void loadUser(String uid) {
        userRepository.getUser(uid).observeForever(user -> _user.setValue(user));
    }

    /**
     * Lưu hồ sơ người dùng.
     */
    public void saveProfile(String uid, String fullName, String phone,
                            String gender, Timestamp birthday,
                            @Nullable String avatarUrl) {
        updateFirestore(uid, fullName, phone, gender, birthday, avatarUrl);
    }

    private void updateFirestore(String uid, String fullName, String phone,
                                  String gender, Timestamp birthday,
                                  @Nullable String avatarUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("phone", phone);
        updates.put("gender", gender);
        updates.put("birthday", birthday);
        if (avatarUrl != null) {
            updates.put("avatarUrl", avatarUrl);
        }

        userRepository.updateUser(uid, updates, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _saveSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.setValue("Lỗi: " + e.getMessage());
            }
        });
    }
}
