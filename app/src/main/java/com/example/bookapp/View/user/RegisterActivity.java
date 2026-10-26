package com.example.bookapp.View.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookapp.R;
import com.example.bookapp.ViewModel.RegisterViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private ProgressBar pbRegister;

    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        pbRegister = findViewById(R.id.pb_register);

        findViewById(R.id.ib_back).setOnClickListener(v -> finish());

        TextView tvLogin = findViewById(R.id.tv_login);
        tvLogin.setOnClickListener(v -> finish()); // quay lại LoginActivity đã mở trước đó

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        viewModel.getRegisterSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                setLoading(false);
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                // Điều hướng thẳng vào HomeActivity vì role chắc chắn là "user"
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                setLoading(false);
                Toast.makeText(this, "Đăng ký thất bại, vui lòng kiểm tra lại", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(fullName) || fullName.length() < 2) {
            etFullName.setError("Họ tên phải có ít nhất 2 ký tự");
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return;
        }
        if (TextUtils.isEmpty(phone) || !phone.matches("^(0|\\+84)[3|5|7|8|9]\\d{8}$")) {
            etPhone.setError("SĐT không hợp lệ (VD: 0912345678)");
            return;
        }
        if (password.length() < 6 || password.length() > 12) {
            etPassword.setError("Mật khẩu phải từ 6-12 ký tự");
            return;
        }
        if (!password.matches(".*[A-Z].*")) {
            etPassword.setError("Mật khẩu phải chứa ít nhất 1 ký tự in hoa");
            return;
        }
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            etPassword.setError("Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        setLoading(true);
        viewModel.register(fullName, email, phone, password);
    }

    private void setLoading(boolean loading) {
        pbRegister.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }
}
