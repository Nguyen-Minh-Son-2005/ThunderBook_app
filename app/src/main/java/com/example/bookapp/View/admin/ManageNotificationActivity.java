package com.example.bookapp.View.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;

import com.example.bookapp.Adapter.admin.AdminSentNotificationAdapter;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.ViewModel.AdminNotificationViewModel;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class ManageNotificationActivity extends AdminBaseActivity {

    private AdminNotificationViewModel viewModel;
    private AdminSentNotificationAdapter sentAdapter;

    private TextInputEditText etTitle, etContent;
    private TextView tvPreviewTitle, tvPreviewContent;
    private ChipGroup cgTarget, cgType;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_manage_notification);

        viewModel = new ViewModelProvider(this).get(AdminNotificationViewModel.class);

        setupToolbar(findViewById(R.id.tb_toolbar), "Gửi thông báo");
        bindViews();
        setupPreview();
        setupRecyclerView();

        findViewById(R.id.btn_send_notification).setOnClickListener(v -> sendNotification());

        viewModel.getSendSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Đã gửi thông báo", Toast.LENGTH_SHORT).show();
                etTitle.setText("");
                etContent.setText("");
                viewModel.loadRecentSent(10).observe(this, sentAdapter::setItems);
            }
        });
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        viewModel.loadRecentSent(10).observe(this, sentAdapter::setItems);
    }

    private void bindViews() {
        etTitle = findViewById(R.id.et_notification_title);
        etContent = findViewById(R.id.et_notification_content);
        tvPreviewTitle = findViewById(R.id.tv_preview_title);
        tvPreviewContent = findViewById(R.id.tv_preview_content);
        cgTarget = findViewById(R.id.cg_notification_target);
        cgType = findViewById(R.id.cg_notification_type);
    }

    private void setupPreview() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        etTitle.addTextChangedListener(watcher);
        etContent.addTextChangedListener(watcher);
    }

    private void updatePreview() {
        String title = etTitle.getText() != null ? etTitle.getText().toString() : "";
        String content = etContent.getText() != null ? etContent.getText().toString() : "";
        tvPreviewTitle.setText(title.isEmpty() ? "Tiêu đề thông báo" : title);
        tvPreviewContent.setText(content.isEmpty() ? "Nội dung thông báo..." : content);
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_sent_notifications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        sentAdapter = new AdminSentNotificationAdapter();
        rv.setAdapter(sentAdapter);
    }

    private void sendNotification() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String content = etContent.getText() != null ? etContent.getText().toString().trim() : "";

        int targetId = cgTarget.getCheckedChipId();
        String type = cgType.getCheckedChipId() == R.id.chip_type_system
                ? Constants.NOTIF_TYPE_SYSTEM : Constants.NOTIF_TYPE_PROMO;

        if (targetId == R.id.chip_target_specific) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Gửi cho một người dùng");
            builder.setMessage("Nhập email người dùng bạn muốn gửi thông báo:");

            final EditText input = new EditText(this);
            input.setHint("VD: user@example.com");
            input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            builder.setView(input);

            builder.setPositiveButton("Gửi", (dialog, which) -> {
                String email = input.getText().toString().trim();
                viewModel.sendToUserEmail(email, title, content, type);
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
            builder.show();
            return;
        }

        // targetUserId = null nghĩa là broadcast cho tất cả user
        viewModel.send(null, title, content, type);
    }
}
