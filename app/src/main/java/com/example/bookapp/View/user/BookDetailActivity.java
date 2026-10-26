package com.example.bookapp.View.user;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookapp.Adapter.user.BookAdapter;
import com.example.bookapp.Adapter.user.BookReviewAdapter;
import com.example.bookapp.Model.Book;
import com.example.bookapp.R;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.ViewModel.BookDetailViewModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";

    private CoordinatorLayout clMainContent;
    private NestedScrollView nsvContent;
    private ProgressBar progressBar;
    
    private ImageView ivCover;
    private TextView tvTitle, tvAuthor, tvCategoryTag, tvRating, tvReviewCount, tvSoldCount,
            tvSalePrice, tvOriginalPrice, tvDiscountPercent, tvStockStatus,
            tvDescription, tvExpandDescription, tvSeeAllReviews, tvReviewSectionTitle, tvQuantity;
    private RecyclerView rvRelatedBooks, rvReviewsPreview;
    private ImageButton ibDecreaseQty, ibIncreaseQty, ibAddToCartIcon;
    private Button btnBuyNow;
    private ExtendedFloatingActionButton fabChatbot;

    private String bookId;
    private Book currentBook;
    private int quantity = 1;
    private boolean isBuyNowMode = false;

    private BookDetailViewModel viewModel;
    
    private BookAdapter relatedBookAdapter;
    private BookReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        bindViews();
        setupClicks();

        viewModel = new ViewModelProvider(this).get(BookDetailViewModel.class);

        viewModel.getBook().observe(this, book -> {
            if (book != null) {
                currentBook = book;
                bindBookData(book);
                String categoryId = (book.getCategoryIds() != null && !book.getCategoryIds().isEmpty()) 
                        ? book.getCategoryIds().get(0) : null;
                viewModel.loadRelatedBooks(bookId, categoryId);
                
                progressBar.setVisibility(View.GONE);
                clMainContent.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getAuthorsText().observe(this, text -> tvAuthor.setText(text));
        
        viewModel.getCategoriesText().observe(this, text -> tvCategoryTag.setText(text));
        
        viewModel.getReviews().observe(this, reviews -> {
            if (reviews != null && !reviews.isEmpty()) {
                reviewAdapter.setReviewList(reviews);
                rvReviewsPreview.setVisibility(View.VISIBLE);
            } else {
                rvReviewsPreview.setVisibility(View.GONE);
            }
        });
        
        viewModel.getRelatedBooks().observe(this, books -> {
            if (books != null) {
                relatedBookAdapter = new BookAdapter(books, book -> {
                    Intent intent = new Intent(this, BookDetailActivity.class);
                    intent.putExtra(EXTRA_BOOK_ID, book.getBookId());
                    startActivity(intent);
                });
                rvRelatedBooks.setAdapter(relatedBookAdapter);
            }
        });

        viewModel.getAddToCartSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                if (isBuyNowMode) {
                    Intent intent = new Intent(this, CheckoutActivity.class);
                    ArrayList<String> ids = new ArrayList<>();
                    ids.add(bookId);
                    intent.putStringArrayListExtra(CheckoutActivity.EXTRA_SELECTED_BOOK_IDS, ids);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
                // Reset state to avoid re-triggering on rotation
                isBuyNowMode = false; 
                viewModel.resetAddToCartStatus();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });

        if (bookId != null) {
            viewModel.incrementViewCount(bookId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bookId != null) {
            progressBar.setVisibility(View.VISIBLE);
            clMainContent.setVisibility(View.INVISIBLE);
            viewModel.loadBook(bookId);
        }
    }

    private void bindViews() {
        clMainContent = findViewById(R.id.cl_main_content);
        nsvContent = findViewById(R.id.nsv_content);
        progressBar = findViewById(R.id.progress_bar);
        
        ivCover = findViewById(R.id.iv_cover);
        tvTitle = findViewById(R.id.tv_title);
        tvAuthor = findViewById(R.id.tv_author);
        tvCategoryTag = findViewById(R.id.tv_category_tag);
        tvRating = findViewById(R.id.tv_rating);
        tvReviewCount = findViewById(R.id.tv_review_count);
        tvSoldCount = findViewById(R.id.tv_sold_count);
        tvSalePrice = findViewById(R.id.tv_sale_price);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        tvDiscountPercent = findViewById(R.id.tv_discount_percent);
        tvStockStatus = findViewById(R.id.tv_stock_status);
        tvDescription = findViewById(R.id.tv_description);
        tvExpandDescription = findViewById(R.id.tv_expand_description);
        tvSeeAllReviews = findViewById(R.id.tv_see_all_reviews);
        tvReviewSectionTitle = findViewById(R.id.tv_review_section_title);
        tvQuantity = findViewById(R.id.tv_quantity);
        rvRelatedBooks = findViewById(R.id.rv_related_books);
        rvReviewsPreview = findViewById(R.id.rv_reviews_preview);
        ibDecreaseQty = findViewById(R.id.ib_decrease_qty);
        ibIncreaseQty = findViewById(R.id.ib_increase_qty);
        ibAddToCartIcon = findViewById(R.id.ib_add_to_cart_icon);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        fabChatbot = findViewById(R.id.fab_chatbot);

        rvRelatedBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        relatedBookAdapter = new BookAdapter(new ArrayList<>(), book -> {});
        rvRelatedBooks.setAdapter(relatedBookAdapter);
        
        rvReviewsPreview.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new BookReviewAdapter(this, new ArrayList<>());
        rvReviewsPreview.setAdapter(reviewAdapter);
    }

    private void setupClicks() {
        tvExpandDescription.setOnClickListener(v -> {
            boolean expanded = tvDescription.getMaxLines() != Integer.MAX_VALUE;
            tvDescription.setMaxLines(expanded ? Integer.MAX_VALUE : 4);
            tvExpandDescription.setText(expanded ? "Thu gọn" : "Xem thêm");
        });

        ibDecreaseQty.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });
        ibIncreaseQty.setOnClickListener(v -> {
            if (currentBook == null || quantity < currentBook.getStock()) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        ibAddToCartIcon.setOnClickListener(v -> {
            isBuyNowMode = false;
            addToCart();
        });
        btnBuyNow.setOnClickListener(v -> {
            isBuyNowMode = true;
            addToCart();
        });

        tvSeeAllReviews.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookReviewsActivity.class);
            intent.putExtra(BookReviewsActivity.EXTRA_BOOK_ID, bookId);
            startActivity(intent);
        });

        if (fabChatbot != null) {
            fabChatbot.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatBotActivity.class);
                intent.putExtra(ChatBotActivity.EXTRA_BOOK_ID, bookId);
                startActivity(intent);
            });
        }
    }

    private void bindBookData(Book book) {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvTitle.setText(book.getTitle());
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", book.getRating()));
        tvReviewCount.setText("(" + book.getRatingCount() + " đánh giá)");
        if (tvReviewSectionTitle != null) {
            tvReviewSectionTitle.setText("Đánh giá (" + book.getRatingCount() + ")");
        }
        tvSoldCount.setText("Đã bán " + book.getSoldCount());
        tvDescription.setText(book.getDescription());

        Glide.with(this).load(book.getCoverImageUrl())
                .placeholder(R.drawable.placeholder_book)
                .into(ivCover);

        if (book.isOnSale()) {
            tvSalePrice.setText(currencyFormat.format(book.getSalePrice()) + "đ");
            tvOriginalPrice.setText(currencyFormat.format(book.getPrice()) + "đ");
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvDiscountPercent.setVisibility(View.VISIBLE);
            int percent = (int) Math.round(100 - (book.getSalePrice() / book.getPrice() * 100));
            tvDiscountPercent.setText("-" + percent + "%");
        } else {
            tvSalePrice.setText(currencyFormat.format(book.getPrice()) + "đ");
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscountPercent.setVisibility(View.GONE);
        }

        boolean inStock = book.getStock() > 0;
        tvStockStatus.setText(inStock ? "Còn hàng" : "Hết hàng");
        btnBuyNow.setEnabled(inStock);
        ibAddToCartIcon.setEnabled(inStock);
    }

    private void addToCart() {
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid == null || currentBook == null) return;

        double priceAtAdd = currentBook.isOnSale() ? currentBook.getSalePrice() : currentBook.getPrice();
        viewModel.addToCart(uid, currentBook.getBookId(), quantity, priceAtAdd);
    }
}
