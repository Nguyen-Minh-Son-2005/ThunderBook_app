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

import com.example.bookapp.Adapter.admin.AdminAuthorAdapter;
import com.example.bookapp.Model.Author;
import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;
import com.example.bookapp.ViewModel.AdminAuthorViewModel;

public class ManageAuthorActivity extends AdminBaseActivity {

    private AdminAuthorViewModel viewModel;
    private AdminAuthorAdapter adapter;
    private View emptyState;
    private TextView tvResultCount;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_manage_author);

        viewModel = new ViewModelProvider(this).get(AdminAuthorViewModel.class);

        setupToolbar(findViewById(R.id.tb_toolbar), "Quản lý tác giả");
        setupSearch();
        setupRecyclerView();

        emptyState = findViewById(R.id.empty_state);
        tvResultCount = findViewById(R.id.tv_result_count);

        findViewById(R.id.fab_add_author).setOnClickListener(v -> openForm(null));

        viewModel.getDisplayedAuthors().observe(this, this::onListChanged);
        viewModel.getErrorMessage().observe(this, this::showError);
        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) Toast.makeText(this, "Đã lưu", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.setHint("Tìm theo tên tác giả");
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
        RecyclerView rv = findViewById(R.id.rv_author_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminAuthorAdapter(new AdminAuthorAdapter.OnAuthorActionListener() {
            @Override
            public void onEditClick(Author author) {
                openForm(author);
            }

            @Override
            public void onDeleteClick(Author author) {
                confirmDelete(author);
            }
        });
        rv.setAdapter(adapter);
    }

    private void openForm(@Nullable Author existing) {
        AdminEntityFormDialog dialog = existing == null
                ? AdminEntityFormDialog.newInstanceAdd("tác giả", false, null)
                : AdminEntityFormDialog.newInstanceEdit("tác giả", false, null,
                existing.getAuthorId(), existing.getName(), null, existing.getAvatarUrl());

        dialog.setOnSaveListener((id, name, description, newImageUri) -> {
            if (newImageUri != null) {
                ImageUtils.uploadImage(ManageAuthorActivity.this, newImageUri, com.example.bookapp.Utils.Constants.STORAGE_AUTHOR_AVATARS,
                        new ImageUtils.OnUploadCompleteListener() {
                            @Override
                            public void onSuccess(String downloadUrl) {
                                viewModel.saveAuthor(id, name, downloadUrl);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(ManageAuthorActivity.this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                String keepImageUrl = existing != null ? existing.getAvatarUrl() : null;
                viewModel.saveAuthor(id, name, keepImageUrl);
            }
        });
        dialog.show(getSupportFragmentManager(), "author_form");
    }

    private void confirmDelete(Author author) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa tác giả")
                .setMessage("Xóa \"" + author.getName() + "\"? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (d, w) -> viewModel.deleteAuthor(author.getAuthorId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void onListChanged(java.util.List<Author> authors) {
        adapter.setItems(authors);
        tvResultCount.setText(authors.size() + " tác giả");
        emptyState.setVisibility(authors.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
