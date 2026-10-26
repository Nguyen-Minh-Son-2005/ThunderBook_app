package com.example.bookapp.Adapter.user;

import android.content.Context;
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

public class BookReviewAdapter extends RecyclerView.Adapter<BookReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private List<Review> reviewList;

    public BookReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.tvUserName.setText(review.getUserName() != null ? review.getUserName() : "Người dùng");
        holder.tvComment.setText(review.getComment());

        if (review.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvReviewDate.setText(sdf.format(review.getCreatedAt().toDate()));
        }

        Glide.with(context)
                .load(review.getUserAvatar())
                .placeholder(R.drawable.placeholder_avatar)
                .circleCrop()
                .into(holder.ivUserAvatar);

        // Hiển thị sao
        int rating = (int) Math.round(review.getRating());
        for (int i = 0; i < holder.llStarsDisplay.getChildCount(); i++) {
            ImageView star = (ImageView) holder.llStarsDisplay.getChildAt(i);
            if (i < rating) {
                star.setColorFilter(context.getResources().getColor(R.color.star_rating, context.getTheme()));
            } else {
                star.setColorFilter(context.getResources().getColor(R.color.divider, context.getTheme()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserName, tvReviewDate, tvComment;
        LinearLayout llStarsDisplay;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            tvComment = itemView.findViewById(R.id.tv_comment);
            llStarsDisplay = itemView.findViewById(R.id.ll_stars_display);
        }
    }
}
