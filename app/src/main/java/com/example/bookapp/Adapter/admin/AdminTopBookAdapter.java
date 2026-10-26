package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Book;
import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_top_book.xml — dùng cho khối "Top sách bán chạy" trên AdminDashboardActivity. */
public class AdminTopBookAdapter extends RecyclerView.Adapter<AdminTopBookAdapter.TopBookViewHolder> {

    public interface OnTopBookClickListener {
        void onBookClick(Book book);
    }

    private List<Book> books = new ArrayList<>();
    private final OnTopBookClickListener listener;

    public AdminTopBookAdapter(OnTopBookClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Book> newItems) {
        this.books = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_top_book, parent, false);
        return new TopBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopBookViewHolder holder, int position) {
        Book book = books.get(position);

        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvTitle.setText(book.getTitle());
        holder.tvSold.setText("Đã bán " + book.getSoldCount());
        ImageUtils.loadImage(holder.ivCover, book.getCoverImageUrl());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBookClick(book);
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class TopBookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvRank, tvTitle, tvSold;

        TopBookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_top_book_cover);
            tvRank = itemView.findViewById(R.id.tv_top_book_rank);
            tvTitle = itemView.findViewById(R.id.tv_top_book_title);
            tvSold = itemView.findViewById(R.id.tv_top_book_sold);
        }
    }
}
