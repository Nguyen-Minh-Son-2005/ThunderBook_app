package com.example.bookapp.View.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.bookapp.R;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.ViewModel.WriteReviewViewModel;

public class WriteReviewActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_BOOK_ID = "extra_book_id";

    private ImageView ivBookCover;
    private TextView tvBookTitle, tvBookAuthor, tvRatingLabel;
    private ImageView[] stars;
    private EditText etComment;
    private Button btnSubmit;

    private String orderId, bookId;
    private int selectedRating = 0;

    private WriteReviewViewModel viewModel;

    private static final String[] RATING_LABELS = {
            "Chạm vào sao để đánh giá", "Rất tệ", "Tệ", "Bình thường", "Tốt", "Xuất sắc"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        bindViews();
        setupStarClicks();

        viewModel = new ViewModelProvider(this).get(WriteReviewViewModel.class);

        viewModel.getBook().observe(this, book -> {
            if (book == null) return;
            tvBookTitle.setText(book.getTitle());
            Glide.with(this).load(book.getCoverImageUrl())
                    .placeholder(R.drawable.placeholder_book)
                    .into(ivBookCover);
            // Tên tác giả cần join thêm từ collection "authors" qua book.getAuthorIds()
            // - lược bớt ở đây
        });

        viewModel.getSubmitSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                // TODO: cập nhật lại rating/ratingCount trung bình của book,
                // nên xử lý bằng Cloud Function trigger onCreate của collection reviews
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                btnSubmit.setEnabled(true);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        if (bookId != null) viewModel.loadBook(bookId);

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void bindViews() {
        ivBookCover = findViewById(R.id.iv_book_cover);
        tvBookTitle = findViewById(R.id.tv_book_title);
        tvBookAuthor = findViewById(R.id.tv_book_author);
        tvRatingLabel = findViewById(R.id.tv_rating_label);
        etComment = findViewById(R.id.et_comment);
        btnSubmit = findViewById(R.id.btn_submit_review);

        stars = new ImageView[]{
                findViewById(R.id.star1), findViewById(R.id.star2), findViewById(R.id.star3),
                findViewById(R.id.star4), findViewById(R.id.star5)
        };
    }

    private void setupStarClicks() {
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i + 1;
            stars[i].setOnClickListener(v -> {
                selectedRating = starIndex;
                updateStarUI();
            });
        }
    }

    private void updateStarUI() {
        int activeColor = ContextCompat.getColor(this, R.color.star_rating);
        int inactiveColor = ContextCompat.getColor(this, R.color.divider);

        for (int i = 0; i < stars.length; i++) {
            stars[i].setColorFilter(i < selectedRating ? activeColor : inactiveColor);
        }
        tvRatingLabel.setText(RATING_LABELS[selectedRating]);
    }

    private void submitReview() {
        if (selectedRating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(comment)) {
            etComment.setError("Vui lòng nhập nhận xét");
            return;
        }

        String uid = FirebaseUtils.getCurrentUserId();
        if (uid == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSubmit.setEnabled(false);
        viewModel.submitReview(uid, bookId, selectedRating, comment, orderId);
    }
}
