package com.example.bookapp.View.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.admin.AdminOrderAdapter;
import com.example.bookapp.Model.User;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.ImageUtils;
import com.example.bookapp.ViewModel.AdminUserDetailViewModel;

public class AdminUserDetailActivity extends AdminBaseActivity {

    private AdminUserDetailViewModel viewModel;
    private AdminOrderAdapter orderAdapter;

    private String userId;
    private ImageView ivAvatar;
    private TextView tvName, tvEmail;
    private SwitchCompat swGrantAdmin;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_admin_user_detail);

        userId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        if (userId == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(AdminUserDetailViewModel.class);

        setupToolbar(findViewById(R.id.tb_toolbar), "Chi tiết người dùng");
        bindViews();
        setupRecyclerView();

        viewModel.loadUser(userId).observe(this, this::onUserLoaded);
        viewModel.loadOrderHistory(userId).observe(this, orderAdapter::setItems);
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
        viewModel.getRoleUpdateSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) Toast.makeText(this, "Đã cập nhật quyền", Toast.LENGTH_SHORT).show();
        });
    }

    private void bindViews() {
        ivAvatar = findViewById(R.id.iv_detail_avatar);
        tvName = findViewById(R.id.tv_detail_name);
        tvEmail = findViewById(R.id.tv_detail_email);
        swGrantAdmin = findViewById(R.id.sw_grant_admin);
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_user_orders);
        rv.setLayoutManager(new LinearLayoutManager(this));
        // Không cần điều hướng tiếp khi bấm đơn ở màn này — chỉ xem nhanh lịch sử.
        orderAdapter = new AdminOrderAdapter(order -> {
        });
        rv.setAdapter(orderAdapter);
    }

    private void onUserLoaded(User user) {
        if (user == null) return;
        tvName.setText(user.getFullName());
        tvEmail.setText(user.getEmail());
        ImageUtils.loadImage(ivAvatar, user.getAvatarUrl(), R.drawable.ic_user);

        // Gỡ listener trước khi setChecked() — tránh switch tự bắn callback với dữ liệu
        // cũ khi Activity mới load xong (đúng lỗi đã tránh ở AdminBookAdapter/AdminVoucherAdapter).
        swGrantAdmin.setOnCheckedChangeListener(null);
        swGrantAdmin.setChecked(Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole()));
        swGrantAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> confirmRoleChange(isChecked));
    }

    /** Thao tác nhạy cảm — LUÔN hỏi xác nhận trước khi gọi ViewModel, đúng lưu ý đã ghi trong Repository. */
    private void confirmRoleChange(boolean grantAdmin) {
        String message = grantAdmin
                ? "Cấp quyền quản trị viên cho tài khoản này?"
                : "Thu hồi quyền quản trị viên của tài khoản này?";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận đổi quyền")
                .setMessage(message)
                .setPositiveButton("Xác nhận", (d, w) -> viewModel.setAdminRole(userId, grantAdmin))
                .setNegativeButton("Hủy", (d, w) -> {
                    // Người dùng hủy — trả switch về trạng thái cũ, tránh hiển thị sai
                    // dù chưa thực sự lưu xuống Firestore.
                    swGrantAdmin.setOnCheckedChangeListener(null);
                    swGrantAdmin.setChecked(!grantAdmin);
                    swGrantAdmin.setOnCheckedChangeListener((btn, checked) -> confirmRoleChange(checked));
                })
                .setCancelable(false)
                .show();
    }
}
