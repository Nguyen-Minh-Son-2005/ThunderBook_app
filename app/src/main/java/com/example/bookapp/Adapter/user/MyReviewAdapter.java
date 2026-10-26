package com.example.bookapp.Adapter.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookapp.Model.Review;
import com.example.bookapp.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewAdapter.MyReviewViewHolder> {

    public interface OnReviewActionListener {
        /** bookCoverUrl được truyền riêng vì Review model không lưu ảnh sách - cần join từ books */
        void onDelete(Review review, int position);
    }

    private final List<Review> reviews;
    /** map tạm bookId -> title/coverUrl, đổ dữ liệu từ ngoài vào sau khi join với collection "books" */
    private final java.util.Map<String, String[]> bookInfoCache; // [title, coverUrl]
    private final OnReviewActionListener listener;

    public MyReviewAdapter(List<Review> reviews, java.util.Map<String, String[]> bookInfoCache,
                            OnReviewActionListener listener) {
        this.reviews = reviews;
        this.bookInfoCache = bookInfoCache;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_review, parent, false);
        return new MyReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        String[] bookInfo = bookInfoCache.get(review.getBookId());
        if (bookInfo != null) {
            holder.tvBookTitle.setText(bookInfo[0]);
            Glide.with(holder.itemView.getContext())
                    .load(bookInfo[1])
                    .placeholder(R.drawable.placeholder_book)
                    .into(holder.ivBookCover);
        }

        if (review.getCreatedAt() != null) {
            holder.tvReviewDate.setText("Đã đánh giá · " + sdf.format(review.getCreatedAt().toDate()));
        }

        holder.tvComment.setText(review.getComment());
        bindStars(holder, (int) review.getRating());

        // Model Review hiện chưa có field adminReply -> luôn ẩn khối này.
        // Nếu sau này bổ sung field adminReply vào Review.java, chỉ cần
        // set text + visibility ở đây là dùng được ngay.
        holder.llAdminReply.setVisibility(View.GONE);

        holder.tvDeleteReview.setOnClickListener(v -> listener.onDelete(review, position));
    }

    private void bindStars(MyReviewViewHolder holder, int rating) {
        for (int i = 0; i < holder.starViews.length; i++) {
            int colorRes = i < rating ? R.color.star_rating : R.color.divider;
            holder.starViews[i].setColorFilter(
                    holder.itemView.getResources().getColor(colorRes));
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class MyReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle, tvReviewDate, tvComment, tvDeleteReview, tvAdminReplyContent;
        LinearLayout llAdminReply, llStarsDisplay;
        ImageView[] starViews;

        MyReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            tvComment = itemView.findViewById(R.id.tv_comment);
            tvDeleteReview = itemView.findViewById(R.id.tv_delete_review);
            llAdminReply = itemView.findViewById(R.id.ll_admin_reply);
            tvAdminReplyContent = itemView.findViewById(R.id.tv_admin_reply_content);
            llStarsDisplay = itemView.findViewById(R.id.ll_stars_display);

            starViews = new ImageView[llStarsDisplay.getChildCount()];
            for (int i = 0; i < llStarsDisplay.getChildCount(); i++) {
                starViews[i] = (ImageView) llStarsDisplay.getChildAt(i);
            }
        }
    }
}
