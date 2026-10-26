package com.example.bookapp.View.user;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.Utils.ImageUtils;
import com.example.bookapp.ViewModel.EditProfileViewModel;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Màn hình chỉnh sửa thông tin cá nhân.
 * Mở từ ProfileFragment khi bấm ib_edit_profile.
 */
public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private ImageButton ibChangeAvatar;
    private EditText etFullName, etEmail, etPhone;
    private TextView etBirthday;
    private View llSelectBirthday;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private Button btnSave;
    private ProgressBar pbSaving;

    private Uri selectedAvatarUri = null;
    private final Calendar birthdayCalendar = Calendar.getInstance();

    private EditProfileViewModel viewModel;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedAvatarUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(ivAvatar);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        bindViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        viewModel.getUser().observe(this, user -> {
            if (user == null) return;

            etFullName.setText(user.getFullName());
            etEmail.setText(user.getEmail());
            etPhone.setText(user.getPhone());

            if ("female".equalsIgnoreCase(user.getGender())) {
                rbFemale.setChecked(true);
            } else if ("other".equalsIgnoreCase(user.getGender())) {
                rbOther.setChecked(true);
            } else {
                rbMale.setChecked(true);
            }

            if (user.getBirthday() != null) {
                birthdayCalendar.setTime(user.getBirthday().toDate());
                updateBirthdayText();
            }

            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Glide.with(this).load(user.getAvatarUrl()).circleCrop().into(ivAvatar);
            }
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                setLoading(false);
                Toast.makeText(this, "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                setLoading(false);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        String uid = FirebaseUtils.getCurrentUserId();
        if (uid != null) viewModel.loadUser(uid);

        ibChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        llSelectBirthday.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void bindViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        ibChangeAvatar = findViewById(R.id.ib_change_avatar);
        etFullName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etBirthday = findViewById(R.id.et_birthday);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        rbOther = findViewById(R.id.rb_other);
        btnSave = findViewById(R.id.btn_save_profile);
        pbSaving = findViewById(R.id.pb_saving);
        llSelectBirthday = findViewById(R.id.ll_select_birthday);
    }

    private void showDatePicker() {
        new DatePickerDialog(this,
                R.style.CustomDatePickerTheme,
                (view, year, month, dayOfMonth) -> {
                    birthdayCalendar.set(year, month, dayOfMonth);
                    updateBirthdayText();
                },
                birthdayCalendar.get(Calendar.YEAR),
                birthdayCalendar.get(Calendar.MONTH),
                birthdayCalendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateBirthdayText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etBirthday.setText(sdf.format(birthdayCalendar.getTime()));
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || fullName.length() < 2) {
            etFullName.setError("Họ tên phải có ít nhất 2 ký tự");
            return;
        }
        if (TextUtils.isEmpty(phone) || !phone.matches("^(0|\\+84)[3|5|7|8|9]\\d{8}$")) {
            etPhone.setError("SĐT không hợp lệ (VD: 0912345678)");
            return;
        }

        String gender = rbFemale.isChecked() ? "female" : rbOther.isChecked() ? "other" : "male";
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid == null) return;

        setLoading(true);
        Timestamp birthday = new Timestamp(birthdayCalendar.getTime());
        
        if (selectedAvatarUri != null) {
            ImageUtils.uploadImage(this, selectedAvatarUri, Constants.STORAGE_AVATARS, new ImageUtils.OnUploadCompleteListener() {
                @Override
                public void onSuccess(String downloadUrl) {
                    viewModel.saveProfile(uid, fullName, phone, gender, birthday, downloadUrl);
                }

                @Override
                public void onFailure(Exception e) {
                    setLoading(false);
                    Toast.makeText(EditProfileActivity.this, "Upload ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            viewModel.saveProfile(uid, fullName, phone, gender, birthday, null);
        }
    }

    private void setLoading(boolean loading) {
        pbSaving.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }
}
