package com.example.bookapp.View.user;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.user.MyReviewAdapter;
import com.example.bookapp.Model.Review;
import com.example.bookapp.R;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.ViewModel.MyReviewsViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn "Đánh giá của tôi" - mở từ ll_menu_reviews trong fragment_profile.xml.
 * Khác với WriteReviewActivity (viết đánh giá mới): đây là xem LẠI các
 * đánh giá mình đã viết trước đó, và cho phép xóa.
 */
public class MyReviewsActivity extends AppCompatActivity {

    private RecyclerView rvMyReviews;
    private LinearLayout llEmpty;

    private MyReviewAdapter adapter;
    private final List<Review> reviewList = new ArrayList<>();
    private final Map<String, String[]> bookInfoCache = new HashMap<>();

    private MyReviewsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvMyReviews = findViewById(R.id.rv_my_reviews);
        llEmpty = findViewById(R.id.ll_empty);

        rvMyReviews.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        adapter = new MyReviewAdapter(reviewList, bookInfoCache,
                (review, position) -> confirmDelete(review, position));
        rvMyReviews.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MyReviewsViewModel.class);

        viewModel.getReviews().observe(this, reviews -> {
            reviewList.clear();
            if (reviews != null) reviewList.addAll(reviews);
            updateEmptyState();
            adapter.notifyDataSetChanged();
        });

        viewModel.getBookInfoCache().observe(this, cache -> {
            if (cache != null) {
                bookInfoCache.clear();
                bookInfoCache.putAll(cache);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getDeleteSuccessPosition().observe(this, position -> {
            if (position != null) {
                adapter.notifyItemRemoved(position);
                updateEmptyState();
            }
        });

        String uid = FirebaseUtils.getCurrentUserId();
        if (uid != null) viewModel.loadReviews(uid);
    }

    private void updateEmptyState() {
        boolean isEmpty = reviewList.isEmpty();
        llEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvMyReviews.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void confirmDelete(Review review, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa đánh giá")
                .setMessage("Bạn có chắc muốn xóa đánh giá này?")
                .setPositiveButton("Xóa", (dialog, which) ->
                        viewModel.deleteReview(review, position))
                .setNegativeButton("Hủy", null)
                .show();
    }
}
