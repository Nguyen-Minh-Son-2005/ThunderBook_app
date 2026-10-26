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


import com.example.bookapp.Utils.DataSeeder;
import com.example.bookapp.Utils.FirebaseCallback;


import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.ViewModel.LoginViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar pbLogin;

    private LoginViewModel viewModel;

    private com.google.android.gms.auth.api.signin.GoogleSignInClient mGoogleSignInClient;
    private androidx.activity.result.ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Seed data 1 lần duy nhất, dùng SharedPreferences để cờ đánh dấu
//        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
//        boolean isSeeded = prefs.getBoolean("is_data_seeded", false);
//        if (!isSeeded) {
//            DataSeeder.seedFullCatalog(new FirebaseCallback<Void>() {
//                @Override
//                public void onSuccess(Void result) {
//                    prefs.edit().putBoolean("is_data_seeded", true).apply();
//                    Toast.makeText(getApplicationContext(), "Seed Data Success!", Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    Toast.makeText(getApplicationContext(), "Seed Data Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            });
//        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        pbLogin = findViewById(R.id.pb_login);

        TextView tvRegister = findViewById(R.id.tv_register);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                navigateByRole();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                setLoading(false);
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });

        btnLogin.setOnClickListener(v -> doLogin());

        // Google Sign-In setup
        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        try {
                            com.google.android.gms.auth.api.signin.GoogleSignInAccount account = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data).getResult(com.google.android.gms.common.api.ApiException.class);
                            if (account != null && account.getIdToken() != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            } else {
                                setLoading(false);
                                Toast.makeText(this, "Không lấy được ID Token. Vui lòng kiểm tra lại cấu hình Web Client ID.", Toast.LENGTH_LONG).show();
                            }
                        } catch (com.google.android.gms.common.api.ApiException e) {
                            setLoading(false);
                            String errorMsg = e.getMessage();
                            if (errorMsg != null && errorMsg.contains("12500")) {
                                Toast.makeText(this, "Lỗi 12500: Bạn chưa thêm mã SHA-1 của máy này vào Firebase Console.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Đăng nhập Google thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        setLoading(false);
                        Toast.makeText(this, "Đăng nhập bị hủy hoặc lỗi cấu hình SHA-1 (Mã: " + result.getResultCode() + ")", Toast.LENGTH_LONG).show();
                    }
                }
        );

        Button btnGoogleLogin = findViewById(R.id.btn_google_login);
        btnGoogleLogin.setOnClickListener(v -> {
            setLoading(true);
            // Đăng xuất phiên cũ (nếu bị kẹt) trước khi hiển thị popup chọn tài khoản
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null);
        viewModel.loginWithGoogle(credential);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Nếu đã đăng nhập từ trước (token còn hiệu lực) -> vào thẳng Home
        if (FirebaseUtils.isLoggedIn()) {
            navigateByRole();
        }
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        setLoading(true);
        viewModel.login(email, password);
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        pbLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }



    /**
     * Sau khi xác thực Firebase thành công (login mới HOẶC session cũ còn hiệu lực ở
     * onStart()), kiểm tra role trước khi quyết định vào HomeActivity (User) hay
     * AdminDashboardActivity (Admin) — đúng thiết kế "1 project, phân luồng bằng role".
     *
     * Dùng RoleChecker.checkRoleSilently() (đã merge từ phía Admin vào RoleChecker.java
     * trước đó) nên KHÔNG cần sửa LoginViewModel — chỉ cần UID hiện tại sau khi
     * FirebaseAuth xác thực xong, hàm này tự query Firestore lấy field role.
     */
    private void navigateByRole() {
        setLoading(true);
        com.example.bookapp.Utils.RoleChecker.checkRoleSilently(this, isAdmin -> {
            setLoading(false);
            if (isAdmin) {
                goToAdminDashboard();
            } else {
                goToHome();
            }
        });
    }

    private void goToAdminDashboard() {
        Intent intent = new Intent(this, com.example.bookapp.View.admin.AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
