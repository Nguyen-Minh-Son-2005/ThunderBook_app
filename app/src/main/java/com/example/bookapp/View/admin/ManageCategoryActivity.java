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

import com.example.bookapp.Adapter.admin.AdminCategoryAdapter;
import com.example.bookapp.Model.Category;
import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;
import com.example.bookapp.ViewModel.AdminCategoryViewModel;

public class ManageCategoryActivity extends AdminBaseActivity {

    private AdminCategoryViewModel viewModel;
    private AdminCategoryAdapter adapter;
    private View emptyState;
    private TextView tvResultCount;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_manage_category);

        viewModel = new ViewModelProvider(this).get(AdminCategoryViewModel.class);

        setupToolbar(findViewById(R.id.tb_toolbar), "Quản lý thể loại");
        setupSearch();
        setupRecyclerView();

        emptyState = findViewById(R.id.empty_state);
        tvResultCount = findViewById(R.id.tv_result_count);

        findViewById(R.id.fab_add_category).setOnClickListener(v -> openForm(null));

        viewModel.getDisplayedCategories().observe(this, this::onListChanged);
        viewModel.getErrorMessage().observe(this, this::showError);
        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) Toast.makeText(this, "Đã lưu", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.setHint("Tìm theo tên thể loại");
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
        RecyclerView rv = findViewById(R.id.rv_category_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCategoryAdapter(new AdminCategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEditClick(Category category) {
                openForm(category);
            }

            @Override
            public void onDeleteClick(Category category) {
                confirmDelete(category);
            }
        });
        rv.setAdapter(adapter);
    }

    private void openForm(@Nullable Category existing) {
        AdminEntityFormDialog dialog = existing == null
                ? AdminEntityFormDialog.newInstanceAdd("thể loại", false, null)
                : AdminEntityFormDialog.newInstanceEdit("thể loại", false, null,
                existing.getCategoryId(), existing.getName(), null, existing.getImageUrl());

        dialog.setOnSaveListener((id, name, description, newImageUri) -> {
            if (newImageUri != null) {
                ImageUtils.uploadImage(ManageCategoryActivity.this, newImageUri, com.example.bookapp.Utils.Constants.STORAGE_CATEGORY_IMAGES,
                        new ImageUtils.OnUploadCompleteListener() {
                            @Override
                            public void onSuccess(String downloadUrl) {
                                viewModel.saveCategory(id, name, downloadUrl);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(ManageCategoryActivity.this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                String keepImageUrl = existing != null ? existing.getImageUrl() : null;
                viewModel.saveCategory(id, name, keepImageUrl);
            }
        });
        dialog.show(getSupportFragmentManager(), "category_form");
    }

    private void confirmDelete(Category category) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa thể loại")
                .setMessage("Xóa \"" + category.getName() + "\"? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (d, w) -> viewModel.deleteCategory(category.getCategoryId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void onListChanged(java.util.List<Category> categories) {
        adapter.setItems(categories);
        tvResultCount.setText(categories.size() + " thể loại");
        emptyState.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
