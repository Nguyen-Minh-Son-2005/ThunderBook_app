package com.example.bookapp.View.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.user.BookAdapter;
import com.example.bookapp.Adapter.user.SearchCategoryAdapter;
import com.example.bookapp.Adapter.user.SearchKeywordAdapter;
import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.Category;
import com.example.bookapp.R;
import com.example.bookapp.ViewModel.SearchViewModel;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_FEATURED = "extra_featured";

    private EditText etSearch;
    private ImageButton ibBack, ibClearSearch;
    private TextView tvCancelSearch, tvClearRecent, tvResultCount, tvEmptySearchMessage,
            tvActiveCategoryChip;
    private ProgressBar pbSearchLoading;
    private LinearLayout llBrowseSection, llResultSection, llEmptySearch,
            llRecentSearches, llActiveFilter, llSearchHeader;
    private RecyclerView rvSearchCategories, rvRecentSearches, rvPopularKeywords, rvSearchResults;
    private ChipGroup cgSortOptions;

    private final List<Category> categoryList = new ArrayList<>();
    private final List<String> recentSearches = new ArrayList<>(); // TODO: lưu SharedPreferences
    private final List<String> popularKeywords = new ArrayList<>();
    private final List<Book> resultBookList = new ArrayList<>();

    private String activeCategoryId = null;
    private boolean isFeaturedOnly = false;

    private SearchViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        activeCategoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        isFeaturedOnly = getIntent().getBooleanExtra(EXTRA_FEATURED, false);

        bindViews();
        setupRecyclerViews();
        setupClicks();

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        viewModel.getCategories().observe(this, categories -> {
            categoryList.clear();
            if (categories != null) categoryList.addAll(categories);
            rvSearchCategories.getAdapter().notifyDataSetChanged();
        });

        viewModel.getSearchResults().observe(this, books -> {
            resultBookList.clear();
            if (books != null) resultBookList.addAll(books);
            rvSearchResults.getAdapter().notifyDataSetChanged();
            tvResultCount.setText(resultBookList.size() + " kết quả");

            if (etSearch.getText().toString().trim().isEmpty() && activeCategoryId == null && !isFeaturedOnly) {
                showBrowseSection();
            } else {
                boolean isEmpty = resultBookList.isEmpty();
                llBrowseSection.setVisibility(View.GONE);
                llResultSection.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                llEmptySearch.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                if (isEmpty) tvEmptySearchMessage.setText("Không tìm thấy kết quả phù hợp");
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                pbSearchLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    llBrowseSection.setVisibility(View.GONE);
                }
            }
        });

        viewModel.loadCategories();
        loadRecentSearches();
        loadPopularKeywords();

        viewModel.setFeaturedOnly(isFeaturedOnly);

        // Luôn luôn khởi tạo lấy toàn bộ sách (hoặc theo danh mục) để chuẩn bị cho Live Search
        viewModel.searchBooks("", activeCategoryId, currentSortKey());

        if (activeCategoryId != null || isFeaturedOnly) {
            if (activeCategoryId != null) showActiveCategoryFilter();
            llBrowseSection.setVisibility(View.GONE);
        } else {
            showBrowseSection();
        }
    }

    private void bindViews() {
        etSearch = findViewById(R.id.et_search);
        ibBack = findViewById(R.id.ib_back);
        ibClearSearch = findViewById(R.id.ib_clear_search);
        tvCancelSearch = findViewById(R.id.tv_cancel_search);
        tvClearRecent = findViewById(R.id.tv_clear_recent);
        tvResultCount = findViewById(R.id.tv_result_count);
        tvEmptySearchMessage = findViewById(R.id.tv_empty_search_message);
        tvActiveCategoryChip = findViewById(R.id.tv_active_category_chip);
        pbSearchLoading = findViewById(R.id.pb_search_loading);
        llBrowseSection = findViewById(R.id.ll_browse_section);
        llResultSection = findViewById(R.id.ll_result_section);
        llEmptySearch = findViewById(R.id.ll_empty_search);
        llRecentSearches = findViewById(R.id.ll_recent_searches);
        llActiveFilter = findViewById(R.id.ll_active_filter);
        llSearchHeader = findViewById(R.id.ll_search_header);
        rvSearchCategories = findViewById(R.id.rv_search_categories);
        rvRecentSearches = findViewById(R.id.rv_recent_searches);
        rvPopularKeywords = findViewById(R.id.rv_popular_keywords);
        rvSearchResults = findViewById(R.id.rv_search_results);
        cgSortOptions = findViewById(R.id.cg_sort_options);
    }

    private void setupRecyclerViews() {
        rvSearchCategories.setLayoutManager(new GridLayoutManager(this, 3));
        rvSearchCategories.setAdapter(new SearchCategoryAdapter(categoryList, category -> {
            activeCategoryId = category.getCategoryId();
            showActiveCategoryFilter();
            viewModel.searchBooks(etSearch.getText().toString().trim(), activeCategoryId, currentSortKey());
        }));

        rvRecentSearches.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRecentSearches.setAdapter(new SearchKeywordAdapter(recentSearches, this::runSearchFromKeyword));

        rvPopularKeywords.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPopularKeywords.setAdapter(new SearchKeywordAdapter(popularKeywords, this::runSearchFromKeyword));

        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 3));
        rvSearchResults.setAdapter(new BookAdapter(resultBookList, book -> {
            String keyword = etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                if (!recentSearches.contains(keyword)) {
                    recentSearches.add(0, keyword);
                    if (recentSearches.size() > 10) {
                        recentSearches.remove(recentSearches.size() - 1);
                    }
                } else {
                    recentSearches.remove(keyword);
                    recentSearches.add(0, keyword);
                }
                saveRecentSearches();
                rvRecentSearches.getAdapter().notifyDataSetChanged();
            }

            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getBookId());
            startActivity(intent);
        }));
    }

    private void setupClicks() {
        ibBack.setOnClickListener(v -> finish());
        tvCancelSearch.setOnClickListener(v -> finish());

        ibClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            showBrowseSection();
        });

        tvClearRecent.setOnClickListener(v -> {
            recentSearches.clear();
            saveRecentSearches();
            rvRecentSearches.getAdapter().notifyDataSetChanged();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                ibClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                String keyword = s.toString().trim();
                if (keyword.isEmpty() && activeCategoryId == null && !isFeaturedOnly) {
                    showBrowseSection();
                } else if (!keyword.isEmpty() || isFeaturedOnly) {
                    llBrowseSection.setVisibility(View.GONE);
                }
                
                // Live Search: cập nhật kết quả ngay khi gõ
                viewModel.updateKeywordLocal(keyword);
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            runSearchFromKeyword(etSearch.getText().toString().trim());
            return true;
        });

        cgSortOptions.setOnCheckedStateChangeListener((group, checkedIds) ->
                viewModel.searchBooks(etSearch.getText().toString().trim(), activeCategoryId, currentSortKey()));

        tvActiveCategoryChip.setOnClickListener(v -> {
            activeCategoryId = null;
            llActiveFilter.setVisibility(View.GONE);
            viewModel.searchBooks(etSearch.getText().toString().trim(), activeCategoryId, currentSortKey());
            
            // Nếu không có keyword và không phải Featured, thì quay về màn hình browse
            if (etSearch.getText().toString().trim().isEmpty() && !isFeaturedOnly) {
                showBrowseSection();
            }
        });
    }

    private void runSearchFromKeyword(String keyword) {
        if (keyword.isEmpty()) return;

        etSearch.setText(keyword);
        etSearch.setSelection(keyword.length());

        if (!recentSearches.contains(keyword)) {
            recentSearches.add(0, keyword);
            if (recentSearches.size() > 10) {
                recentSearches.remove(recentSearches.size() - 1);
            }
        } else {
            recentSearches.remove(keyword);
            recentSearches.add(0, keyword);
        }
        saveRecentSearches();
        rvRecentSearches.getAdapter().notifyDataSetChanged();

        viewModel.searchBooks(keyword, activeCategoryId, currentSortKey());
    }

    private String currentSortKey() {
        int checkedId = cgSortOptions.getCheckedChipId();
        if (checkedId == R.id.chip_sort_price_asc) return "price_asc";
        if (checkedId == R.id.chip_sort_price_desc) return "price_desc";
        if (checkedId == R.id.chip_sort_rating) return "rating";
        if (checkedId == R.id.chip_sort_bestseller) return "bestseller";
        return "relevance";
    }

    private void showActiveCategoryFilter() {
        for (Category category : categoryList) {
            if (category.getCategoryId().equals(activeCategoryId)) {
                tvActiveCategoryChip.setText(category.getName() + "  ✕");
                break;
            }
        }
        llActiveFilter.setVisibility(View.VISIBLE);
    }

    private void showBrowseSection() {
        llBrowseSection.setVisibility(View.VISIBLE);
        llResultSection.setVisibility(View.GONE);
        llEmptySearch.setVisibility(View.GONE);
    }

    /**
     * TODO: thay danh sách mẫu này bằng thống kê từ khóa được tìm nhiều nhất thật
     * (vd 1 collection "searchStats" tăng dần counter mỗi lần searchBooks() chạy).
     */
    private void loadPopularKeywords() {
        popularKeywords.add("Đắc Nhân Tâm");
        popularKeywords.add("Nhà Giả Kim");
        popularKeywords.add("Tư duy nhanh và chậm");
        popularKeywords.add("Sách thiếu nhi");
        rvPopularKeywords.getAdapter().notifyDataSetChanged();
    }

    private void saveRecentSearches() {
        android.content.SharedPreferences prefs = getSharedPreferences("recent_searches_prefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recentSearches.size(); i++) {
            sb.append(recentSearches.get(i));
            if (i < recentSearches.size() - 1) sb.append("|||");
        }
        editor.putString("recent_searches", sb.toString());
        editor.apply();
    }

    private void loadRecentSearches() {
        android.content.SharedPreferences prefs = getSharedPreferences("recent_searches_prefs", MODE_PRIVATE);
        String saved = prefs.getString("recent_searches", "");
        recentSearches.clear();
        if (!saved.isEmpty()) {
            String[] arr = saved.split("\\|\\|\\|");
            for (String s : arr) {
                if (!s.trim().isEmpty()) recentSearches.add(s.trim());
            }
        }
    }
}
