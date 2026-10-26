package com.example.bookapp.View.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookapp.Adapter.admin.AdminBookAdapter;
import com.example.bookapp.Model.Author;
import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.Category;
import com.example.bookapp.Model.Publisher;
import com.example.bookapp.R;
import com.example.bookapp.Repository.AdminAuthorRepository;
import com.example.bookapp.Repository.AdminCategoryRepository;
import com.example.bookapp.Repository.AdminPublisherRepository;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.ViewModel.AdminBookViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ManageBookActivity extends AdminBaseActivity {

    private AdminBookViewModel viewModel;
    private AdminBookAdapter adapter;

    private View emptyState;
    private TextView tvResultCount;
    private final AdminCategoryRepository categoryRepository = new AdminCategoryRepository();
    private final AdminAuthorRepository authorRepository = new AdminAuthorRepository();
    private final AdminPublisherRepository publisherRepository = new AdminPublisherRepository();

    private List<Category> cachedCategories;
    private List<Author> cachedAuthors;
    private List<Publisher> cachedPublishers;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_manage_book);

        viewModel = new ViewModelProvider(this).get(AdminBookViewModel.class);

        Toolbar toolbar = findViewById(R.id.tb_toolbar);
        setupToolbar(toolbar, "Quản lý sách");

        setupSearchBar();
        setupFilterChips();
        setupRecyclerView();
        setupFab();
        setupSwipeRefresh();

        emptyState = findViewById(R.id.empty_state);
        tvResultCount = findViewById(R.id.tv_result_count);

        viewModel.getDisplayedBooks().observe(this, this::onBooksChanged);
        viewModel.getErrorMessage().observe(this, this::showError);

        categoryRepository.observeAllCategories().observe(this, list -> cachedCategories = list);
        authorRepository.observeAllAuthors().observe(this, list -> cachedAuthors = list);
        publisherRepository.observeAllPublishers().observe(this, list -> cachedPublishers = list);
    }

    private void setupSearchBar() {
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.setHint("Tìm theo tên sách");
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

        FrameLayout flSort = findViewById(R.id.fl_sort);
        flSort.setOnClickListener(this::showSortMenu);
    }

    private void showSortMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, "Mới nhất");
        menu.getMenu().add(0, 2, 1, "Giá tăng dần");
        menu.getMenu().add(0, 3, 2, "Giá giảm dần");
        menu.getMenu().add(0, 4, 3, "Bán chạy nhất");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    viewModel.setSortOption(AdminBookViewModel.SortOption.NEWEST);
                    break;
                case 2:
                    viewModel.setSortOption(AdminBookViewModel.SortOption.PRICE_ASC);
                    break;
                case 3:
                    viewModel.setSortOption(AdminBookViewModel.SortOption.PRICE_DESC);
                    break;
                case 4:
                    viewModel.setSortOption(AdminBookViewModel.SortOption.BEST_SELLING);
                    break;
            }
            return true;
        });
        menu.show();
    }

    private void setupFilterChips() {
        ChipGroup chipGroup = findViewById(R.id.cg_book_filter);
        chipGroup.setSingleSelection(true); // ép chọn 1 trong 4, dù XML có khai hay chưa

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_in_stock) {
                viewModel.setStockFilter(AdminBookViewModel.StockFilter.IN_STOCK);
            } else if (id == R.id.chip_out_stock) {
                viewModel.setStockFilter(AdminBookViewModel.StockFilter.OUT_OF_STOCK);
            } else if (id == R.id.chip_hidden) {
                viewModel.setStockFilter(AdminBookViewModel.StockFilter.HIDDEN);
            } else if (id == R.id.chip_all) {
                viewModel.setStockFilter(AdminBookViewModel.StockFilter.ALL);
            }
        });

        Chip chipCategory = findViewById(R.id.chip_category_filter);
        chipCategory.setOnClickListener(v -> showCategoryFilterMenu(v));

        Chip chipAuthor = findViewById(R.id.chip_author_filter);
        chipAuthor.setOnClickListener(v -> showAuthorFilterMenu(v));

        Chip chipPublisher = findViewById(R.id.chip_publisher_filter);
        chipPublisher.setOnClickListener(v -> showPublisherFilterMenu(v));
    }

    private void showCategoryFilterMenu(View anchor) {
        if (cachedCategories == null || cachedCategories.isEmpty()) return;
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 0, 0, "Tất cả thể loại");
        for (int i = 0; i < cachedCategories.size(); i++) {
            menu.getMenu().add(0, i + 1, i + 1, cachedCategories.get(i).getName());
        }
        menu.setOnMenuItemClickListener(item -> {
            int which = item.getItemId();
            if (which == 0) {
                viewModel.setCategoryFilter(null);
            } else {
                viewModel.setCategoryFilter(cachedCategories.get(which - 1).getCategoryId());
            }
            return true;
        });
        menu.show();
    }

    private void showAuthorFilterMenu(View anchor) {
        if (cachedAuthors == null || cachedAuthors.isEmpty()) return;
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 0, 0, "Tất cả tác giả");
        for (int i = 0; i < cachedAuthors.size(); i++) {
            menu.getMenu().add(0, i + 1, i + 1, cachedAuthors.get(i).getName());
        }
        menu.setOnMenuItemClickListener(item -> {
            int which = item.getItemId();
            if (which == 0) {
                viewModel.setAuthorFilter(null);
            } else {
                viewModel.setAuthorFilter(cachedAuthors.get(which - 1).getAuthorId());
            }
            return true;
        });
        menu.show();
    }

    private void showPublisherFilterMenu(View anchor) {
        if (cachedPublishers == null || cachedPublishers.isEmpty()) return;
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 0, 0, "Tất cả NXB");
        for (int i = 0; i < cachedPublishers.size(); i++) {
            menu.getMenu().add(0, i + 1, i + 1, cachedPublishers.get(i).getName());
        }
        menu.setOnMenuItemClickListener(item -> {
            int which = item.getItemId();
            if (which == 0) {
                viewModel.setPublisherFilter(null);
            } else {
                viewModel.setPublisherFilter(cachedPublishers.get(which - 1).getPublisherId());
            }
            return true;
        });
        menu.show();
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_book_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminBookAdapter(new AdminBookAdapter.OnBookActionListener() {
            @Override
            public void onItemClick(Book book) {
                openEditBook(book.getBookId());
            }

            @Override
            public void onMenuClick(Book book, View anchorView) {
                showItemMenu(book, anchorView);
            }

            @Override
            public void onToggleActive(Book book, boolean newActiveState) {
                viewModel.toggleBookActive(book.getBookId(), newActiveState);
            }
        });
        rv.setAdapter(adapter);
    }

    private void showItemMenu(Book book, View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, "Sửa");
        menu.getMenu().add(0, 2, 1, "Xóa");
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                openEditBook(book.getBookId());
            } else if (item.getItemId() == 2) {
                confirmDelete(book);
            }
            return true;
        });
        menu.show();
    }

    private void confirmDelete(Book book) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa sách")
                .setMessage("Ẩn \"" + book.getTitle() + "\" khỏi cửa hàng? Sách vẫn giữ trong hệ thống để không ảnh hưởng đơn hàng cũ.")
                .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteBook(book.getBookId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void openEditBook(String bookId) {
        Intent intent = new Intent(this, AddEditBookActivity.class);
        intent.putExtra(Constants.EXTRA_BOOK_ID, bookId);
        intent.putExtra(Constants.EXTRA_MODE_EDIT, true);
        startActivity(intent);
    }

    private void setupFab() {
        findViewById(R.id.fab_add_book).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditBookActivity.class)));
    }

    private void setupSwipeRefresh() {
        SwipeRefreshLayout srl = findViewById(R.id.srl_refresh);
        // Dữ liệu đã realtime qua LiveData/Firestore listener — kéo để làm mới chỉ mang
        // tính cảm giác quen thuộc cho người dùng, không cần gọi lại API thủ công.
        srl.setOnRefreshListener(() -> srl.setRefreshing(false));
    }

    private void onBooksChanged(List<Book> books) {
        findViewById(R.id.pb_loading).setVisibility(View.GONE);
        adapter.setItems(books);
        tvResultCount.setText(books.size() + " sách");
        emptyState.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message != null) {
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
