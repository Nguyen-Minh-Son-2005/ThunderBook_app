package com.example.bookapp.View.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.admin.AdminPublisherAdapter;
import com.example.bookapp.Model.Publisher;
import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;
import com.example.bookapp.ViewModel.AdminPublisherViewModel;

public class ManagePublisherActivity extends AdminBaseActivity {

    private AdminPublisherViewModel viewModel;
    private AdminPublisherAdapter adapter;
    private View emptyState;
    private TextView tvResultCount;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_manage_publisher);

        viewModel = new ViewModelProvider(this).get(AdminPublisherViewModel.class);

        setupToolbar(findViewById(R.id.tb_toolbar), "Quản lý nhà xuất bản");
        setupSearch();
        setupRecyclerView();

        emptyState = findViewById(R.id.empty_state);
        tvResultCount = findViewById(R.id.tv_result_count);

        findViewById(R.id.fab_add_publisher).setOnClickListener(v -> openForm(null));

        viewModel.getDisplayedPublishers().observe(this, this::onListChanged);
        viewModel.getErrorMessage().observe(this, this::showError);
        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) Toast.makeText(this, "Đã lưu", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.setHint("Tìm theo tên nhà xuất bản");
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // Chỉ có 1 tiêu chí sắp xếp thực chất (A-Z theo Firestore orderBy sẵn) nên ẩn nút sort.
        FrameLayout flSort = findViewById(R.id.fl_sort);
        flSort.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_publisher_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminPublisherAdapter(new AdminPublisherAdapter.OnPublisherActionListener() {
            @Override
            public void onEditClick(Publisher publisher) {
                openForm(publisher);
            }

            @Override
            public void onDeleteClick(Publisher publisher) {
                confirmDelete(publisher);
            }
        });
        rv.setAdapter(adapter);
    }

    private void openForm(@Nullable Publisher existing) {
        AdminEntityFormDialog dialog = existing == null
                ? AdminEntityFormDialog.newInstanceAdd("nhà xuất bản", false, null)
                : AdminEntityFormDialog.newInstanceEdit("nhà xuất bản", false, null,
                existing.getPublisherId(), existing.getName(), null, existing.getLogoUrl());

        dialog.setOnSaveListener((id, name, description, newImageUri) -> {
            if (newImageUri != null) {
                ImageUtils.uploadImage(ManagePublisherActivity.this, newImageUri, com.example.bookapp.Utils.Constants.STORAGE_PUBLISHER_LOGOS,
                        new ImageUtils.OnUploadCompleteListener() {
                            @Override
                            public void onSuccess(String downloadUrl) {
                                viewModel.savePublisher(id, name, downloadUrl);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(ManagePublisherActivity.this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                String keepImageUrl = existing != null ? existing.getLogoUrl() : null;
                viewModel.savePublisher(id, name, keepImageUrl);
            }
        });
        dialog.show(getSupportFragmentManager(), "publisher_form");
    }

    private void confirmDelete(Publisher publisher) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa nhà xuất bản")
                .setMessage("Xóa \"" + publisher.getName() + "\"? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (d, w) -> viewModel.deletePublisher(publisher.getPublisherId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void onListChanged(java.util.List<Publisher> publishers) {
        adapter.setItems(publishers);
        tvResultCount.setText(publishers.size() + " nhà xuất bản");
        emptyState.setVisibility(publishers.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
