package com.example.bookapp.View.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.user.BookReviewAdapter;
import com.example.bookapp.R;
import com.example.bookapp.ViewModel.BookReviewsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class BookReviewsActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";

    private String bookId;
    private BookReviewsViewModel viewModel;
    private BookReviewAdapter reviewAdapter;

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RecyclerView rvReviews;
    private FloatingActionButton fabWriteReview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reviews);

        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        rvReviews = findViewById(R.id.rv_reviews);
        fabWriteReview = findViewById(R.id.fab_write_review);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new BookReviewAdapter(this, new ArrayList<>());
        rvReviews.setAdapter(reviewAdapter);

        // Ẩn mặc định, đợi ViewModel kiểm tra xem đã mua sách chưa
        fabWriteReview.setVisibility(View.GONE);
        fabWriteReview.setOnClickListener(v -> {
            Intent intent = new Intent(this, WriteReviewActivity.class);
            intent.putExtra(WriteReviewActivity.EXTRA_BOOK_ID, bookId);
            startActivity(intent);
        });

        viewModel = new ViewModelProvider(this).get(BookReviewsViewModel.class);

        viewModel.getCanReview().observe(this, canReview -> {
            if (Boolean.TRUE.equals(canReview)) {
                fabWriteReview.setVisibility(View.VISIBLE);
            } else {
                fabWriteReview.setVisibility(View.GONE);
            }
        });

        viewModel.getReviews().observe(this, reviews -> {
            progressBar.setVisibility(View.GONE);
            if (reviews != null && !reviews.isEmpty()) {
                reviewAdapter.setReviewList(reviews);
                rvReviews.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                rvReviews.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bookId != null) {
            progressBar.setVisibility(View.VISIBLE);
            viewModel.loadReviews(bookId);
            viewModel.checkCanReview(bookId);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }
}
