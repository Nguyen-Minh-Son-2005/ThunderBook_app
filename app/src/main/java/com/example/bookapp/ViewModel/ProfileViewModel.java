package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.User;
import com.example.bookapp.Repository.UserRepository;

/**
 * ViewModel cho ProfileFragment.
 * Tách loadUserInfo() ra khỏi Fragment.
 */
public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository = new UserRepository();

    private final MutableLiveData<User> _user = new MutableLiveData<>();

    public LiveData<User> getUser() { return _user; }

    public void loadUser(String uid) {
        userRepository.getUser(uid).observeForever(user -> _user.setValue(user));
    }
}
