package com.example.bookapp.Adapter.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookapp.Model.Book;
import com.example.bookapp.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Adapter hiển thị danh sách thẻ sách gợi ý nằm ngang bên trong tin nhắn Bot.
 */
public class ChatBookSuggestionAdapter extends RecyclerView.Adapter<ChatBookSuggestionAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    private final Context context;
    private final List<Book> books;
    private final OnBookClickListener listener;
    private final DecimalFormat priceFormatter = new DecimalFormat("#,###đ");

    public ChatBookSuggestionAdapter(Context context, List<Book> books, OnBookClickListener listener) {
        this.context = context;
        this.books = books;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_book_suggestion, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        if (book == null) return;

        holder.tvTitle.setText(book.getTitle());
        holder.tvRating.setText(String.format("%.1f", book.getRating()));

        double effectivePrice = book.getSalePrice() > 0 ? book.getSalePrice() : book.getPrice();
        holder.tvPrice.setText(priceFormatter.format(effectivePrice));

        if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getCoverImageUrl())
                    .placeholder(R.drawable.placeholder_book)
                    .error(R.drawable.placeholder_book)
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.placeholder_book);
        }

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.btnViewDetail.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvRating, tvPrice, btnViewDetail;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvRating = itemView.findViewById(R.id.tv_book_rating);
            tvPrice = itemView.findViewById(R.id.tv_book_price);
            btnViewDetail = itemView.findViewById(R.id.btn_view_detail);
        }
    }
}
