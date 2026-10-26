package com.example.bookapp.View.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookapp.R;
import com.example.bookapp.Utils.RoleChecker;

/**
 * Mọi Activity trong View/admin đều extends class này thay vì AppCompatActivity trực tiếp
 * — tự động kiểm tra quyền admin ngay khi mở màn (RoleChecker.checkAdminPermission()),
 * tự Toast + finish() nếu không phải admin, KHÔNG cần mỗi Activity tự viết lại đoạn này.
 *
 * Cách dùng trong Activity con:
 * <pre>
 * public class ManageBookActivity extends AdminBaseActivity {
 *     protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
 *         setContentView(R.layout.admin_activity_manage_book);
 *         Toolbar toolbar = findViewById(R.id.tb_toolbar);
 *         setupToolbar(toolbar, "Quản lý sách");
 *         // ... phần còn lại của onCreate
 *     }
 * }
 * </pre>
 *
 * LƯU Ý: KHÔNG override onCreate() nữa — mọi logic khởi tạo màn hình đưa vào
 * onAdminAccessGranted(), chỉ chạy SAU KHI xác nhận đúng quyền admin.
 */
public abstract class AdminBaseActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            RoleChecker.checkAdminPermission(this, () -> {
                try {
                    onAdminAccessGranted(savedInstanceState);
                } catch (Exception e) {
                    Toast.makeText(this, "Crash in onAdminAccessGranted: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Crash in checkAdminPermission: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /** Chỉ chạy khi đã xác nhận user hiện tại có role = admin. */
    protected abstract void onAdminAccessGranted(@Nullable Bundle savedInstanceState);

    /** Gọi trong onAdminAccessGranted() sau setContentView() — set tiêu đề + nút back. */
    protected void setupToolbar(Toolbar toolbar, String title) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        TextView tvTitle = toolbar.findViewById(R.id.tv_toolbar_title);
        if (tvTitle != null) tvTitle.setText(title);
    }
}
