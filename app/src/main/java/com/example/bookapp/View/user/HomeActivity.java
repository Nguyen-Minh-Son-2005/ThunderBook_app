package com.example.bookapp.View.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.bookapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity "khung" chứa Bottom Navigation, hoán đổi giữa 3 Fragment
 * (Home / Cart / Profile) trong fl_container. Riêng nav_search mở thẳng
 * SearchActivity (full-screen), không phải Fragment.
 */
public class HomeActivity extends AppCompatActivity {

    private final HomeFragment homeFragment = new HomeFragment();
    private final CartFragment cartFragment = new CartFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        com.example.bookapp.Repository.FavoriteRepository.loadFavorites();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            switchFragment(homeFragment);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                // Không switchFragment vì Search là màn riêng - giữ nguyên tab đang chọn.
                return false;
            } else if (id == R.id.nav_cart) {
                switchFragment(cartFragment);
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });
    }

    private void switchFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container, fragment)
                .commit();
    }
}
