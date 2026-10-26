package com.example.bookapp.View.admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookapp.Model.Author;
import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.Category;
import com.example.bookapp.Model.Publisher;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.ImageUtils;
import com.example.bookapp.ViewModel.AdminAddEditBookViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditBookActivity extends AdminBaseActivity {

    private AdminAddEditBookViewModel viewModel;

    private TextInputEditText etTitle, etDescription, etPrice, etSalePrice, etStock;
    private AutoCompleteTextView actAuthor, actPublisher;
    private ChipGroup cgCategories;
    private ImageView ivCoverPreview;
    private View uploadPlaceholder;
    private androidx.appcompat.widget.SwitchCompat swFeatured, swActive;
    private com.google.android.material.button.MaterialButton btnSave, btnDelete;

    private String editingBookId = null;
    private String uploadedCoverUrl = null;
    private Uri pendingImageUri = null;

    // name -> id, dùng để tra ngược khi lưu (AutoCompleteTextView chỉ lưu được text hiển thị)
    private final Map<String, String> authorNameToId = new HashMap<>();
    private final Map<String, String> publisherNameToId = new HashMap<>();
    private List<Category> allCategories = new ArrayList<>();

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                pendingImageUri = uri;
                ivCoverPreview.setImageURI(uri);
                ivCoverPreview.setVisibility(View.VISIBLE);
                uploadPlaceholder.setVisibility(View.GONE);
            });

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_add_edit_book);

        viewModel = new ViewModelProvider(this).get(AdminAddEditBookViewModel.class);

        bindViews();
        setupToolbar(findViewById(R.id.tb_toolbar), "Thêm sách");
        setupCoverUpload();
        observeDropdownData();

        editingBookId = getIntent().getStringExtra(Constants.EXTRA_BOOK_ID);
        boolean isEditMode = getIntent().getBooleanExtra(Constants.EXTRA_MODE_EDIT, false);
        if (isEditMode && editingBookId != null) {
            setupToolbar(findViewById(R.id.tb_toolbar), "Sửa sách");
            btnDelete.setVisibility(View.VISIBLE);
            viewModel.loadBookForEdit(editingBookId);
        }

        observeViewModel();

        btnSave.setOnClickListener(v -> saveBook());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void bindViews() {
        etTitle = findViewById(R.id.et_book_title);
        etDescription = findViewById(R.id.et_book_description);
        etPrice = findViewById(R.id.et_book_price);
        etSalePrice = findViewById(R.id.et_book_sale_price);
        etStock = findViewById(R.id.et_book_stock);
        actAuthor = findViewById(R.id.act_book_author);
        actPublisher = findViewById(R.id.act_book_publisher);
        cgCategories = findViewById(R.id.cg_book_categories);
        ivCoverPreview = findViewById(R.id.iv_book_cover_preview);
        uploadPlaceholder = findViewById(R.id.ll_upload_placeholder);
        swFeatured = findViewById(R.id.sw_is_featured);
        swActive = findViewById(R.id.sw_is_active);
        btnSave = findViewById(R.id.btn_save_book);
        btnDelete = findViewById(R.id.btn_delete_book);
    }

    private void setupCoverUpload() {
        FrameLayout flUpload = findViewById(R.id.fl_cover_upload);
        flUpload.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void observeDropdownData() {
        viewModel.getAuthors().observe(this, authors -> {
            authorNameToId.clear();
            List<String> names = new ArrayList<>();
            for (Author a : authors) {
                authorNameToId.put(a.getName(), a.getAuthorId());
                names.add(a.getName());
            }
            actAuthor.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names));
        });

        viewModel.getPublishers().observe(this, publishers -> {
            publisherNameToId.clear();
            List<String> names = new ArrayList<>();
            for (Publisher p : publishers) {
                publisherNameToId.put(p.getName(), p.getPublisherId());
                names.add(p.getName());
            }
            actPublisher.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names));
        });

        viewModel.getCategories().observe(this, categories -> {
            allCategories = categories;
            cgCategories.removeAllViews();
            for (Category c : categories) {
                Chip chip = (Chip) LayoutInflater.from(this)
                        .inflate(R.layout.admin_item_category_chip, cgCategories, false);
                chip.setText(c.getName());
                chip.setTag(c.getCategoryId());
                cgCategories.addView(chip);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getLoadedBook().observe(this, this::populateForm);

        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Đã lưu sách", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        viewModel.getIsLoading().observe(this, loading ->
                btnSave.setEnabled(!Boolean.TRUE.equals(loading)));
    }

    private void populateForm(Book book) {
        if (book == null) return;
        etTitle.setText(book.getTitle());
        etDescription.setText(book.getDescription());
        etPrice.setText(String.valueOf((long) book.getPrice()));
        etSalePrice.setText(book.getSalePrice() > 0 ? String.valueOf((long) book.getSalePrice()) : "");
        etStock.setText(String.valueOf(book.getStock()));
        swFeatured.setChecked(book.isFeatured());
        swActive.setChecked(book.isActive());
        uploadedCoverUrl = book.getCoverImageUrl();
        ImageUtils.loadImage(ivCoverPreview, book.getCoverImageUrl());
        ivCoverPreview.setVisibility(View.VISIBLE);
        uploadPlaceholder.setVisibility(View.GONE);

        // Tick sẵn các chip thể loại đã chọn — chạy sau khi cả loadedBook và categories đều
        // đã về, nên gọi lại applyPreselectedCategories() ở đây, chip đã add xong từ trước.
        if (book.getCategoryIds() != null) {
            for (int i = 0; i < cgCategories.getChildCount(); i++) {
                Chip chip = (Chip) cgCategories.getChildAt(i);
                if (book.getCategoryIds().contains((String) chip.getTag())) {
                    chip.setChecked(true);
                }
            }
        }

        // Chọn sẵn tác giả đầu tiên trong danh sách (UI hiện chỉ hỗ trợ 1 tác giả/sách dù
        // Model cho phép nhiều — xem ghi chú trong saveBook() bên dưới).
        if (book.getAuthorIds() != null && !book.getAuthorIds().isEmpty()) {
            for (Map.Entry<String, String> entry : authorNameToId.entrySet()) {
                if (entry.getValue().equals(book.getAuthorIds().get(0))) {
                    actAuthor.setText(entry.getKey(), false);
                    break;
                }
            }
        }
        for (Map.Entry<String, String> entry : publisherNameToId.entrySet()) {
            if (entry.getValue().equals(book.getPublisherId())) {
                actPublisher.setText(entry.getKey(), false);
                break;
            }
        }
    }

    private void saveBook() {
        String title = safeText(etTitle);
        String description = safeText(etDescription);
        String authorName = actAuthor.getText().toString().trim();
        String publisherName = actPublisher.getText().toString().trim();

        String authorId = authorNameToId.get(authorName);
        String publisherId = publisherNameToId.get(publisherName);

        List<String> categoryIds = new ArrayList<>();
        for (int i = 0; i < cgCategories.getChildCount(); i++) {
            Chip chip = (Chip) cgCategories.getChildAt(i);
            if (chip.isChecked()) categoryIds.add((String) chip.getTag());
        }

        double price = parseDoubleSafe(safeText(etPrice));
        double salePrice = parseDoubleSafe(safeText(etSalePrice));
        int stock = (int) parseDoubleSafe(safeText(etStock));

        // UI chỉ hỗ trợ chọn 1 tác giả — bọc vào List vì Model Book.authorIds là mảng.
        // Dùng ArrayList thay vì List.of() vì List.of() yêu cầu minSdk 26+, không chắc
        // project đang để minSdk bao nhiêu.
        List<String> authorIds = new ArrayList<>();
        if (authorId != null) authorIds.add(authorId);

        Runnable doSave = () -> viewModel.saveBook(editingBookId, title, description, uploadedCoverUrl,
                authorIds, publisherId, categoryIds, price, salePrice, stock,
                swFeatured.isChecked(), swActive.isChecked());

        if (pendingImageUri != null) {
            ImageUtils.uploadImage(this, pendingImageUri, Constants.STORAGE_BOOK_COVERS, new ImageUtils.OnUploadCompleteListener() {
                @Override
                public void onSuccess(String downloadUrl) {
                    uploadedCoverUrl = downloadUrl;
                    doSave.run();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(AddEditBookActivity.this, "Upload ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            doSave.run();
        }
    }

    private void confirmDelete() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa sách")
                .setMessage("Bạn chắc chắn muốn ẩn sách này khỏi cửa hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (editingBookId != null) viewModel.deleteBook(editingBookId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String safeText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private double parseDoubleSafe(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
