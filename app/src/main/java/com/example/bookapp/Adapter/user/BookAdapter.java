package com.example.bookapp.Adapter.user;

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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Dùng chung cho rv_featured_books / rv_all_books (fragment_home.xml)
 * và rv_search_results (activity_search.xml) - đều tái sử dụng item_book.xml.
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onClick(Book book);
    }

    private final List<Book> books;
    private final OnBookClickListener listener;

    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);

        // Chỉnh cho item tự động căn giữa và lấp đầy khi dùng trong GridLayout (2 cột)
        if (parent instanceof RecyclerView) {
            RecyclerView.LayoutManager lm = ((RecyclerView) parent).getLayoutManager();
            if (lm instanceof androidx.recyclerview.widget.GridLayoutManager) {
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                view.setLayoutParams(lp);
            }
        }

        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        holder.tvTitle.setText(book.getTitle());
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f", book.getRating()));

        Glide.with(holder.itemView.getContext())
                .load(book.getCoverImageUrl())
                .placeholder(R.drawable.placeholder_book)
                .into(holder.ivCover);

        if (book.isOnSale()) {
            holder.tvSalePrice.setText(currencyFormat.format(book.getSalePrice()) + "đ");
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText(currencyFormat.format(book.getPrice()) + "đ");
            holder.tvOriginalPrice.setPaintFlags(
                    holder.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvSaleBadge.setVisibility(View.VISIBLE);
            int percent = (int) Math.round(100 - (book.getSalePrice() / book.getPrice() * 100));
            holder.tvSaleBadge.setText("-" + percent + "%");
        } else {
            holder.tvSalePrice.setText(currencyFormat.format(book.getPrice()) + "đ");
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.tvSaleBadge.setVisibility(View.GONE);
        }

        // Hiển thị tên tác giả đã được join trong ViewModel
        String authorName = book.getAuthorNameDisplay();
        if (authorName == null || authorName.isEmpty()) {
            holder.tvAuthor.setText("Đang cập nhật...");
        } else {
            holder.tvAuthor.setText(authorName);
        }

        // Logic Yêu thích (Favorite)
        java.util.Set<String> favs = com.example.bookapp.Repository.FavoriteRepository.getFavoriteBookIds().getValue();
        boolean isFav = favs != null && favs.contains(book.getBookId());

        holder.ibFavorite.setColorFilter(
                androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                        isFav ? R.color.accent_price : R.color.text_secondary)
        );

        holder.ibFavorite.setOnClickListener(v -> {
            boolean currentlyFav = favs != null && favs.contains(book.getBookId());
            boolean newFav = !currentlyFav;
            
            if (favs != null) {
                if (newFav) favs.add(book.getBookId());
                else favs.remove(book.getBookId());
            }
            
            holder.ibFavorite.setColorFilter(
                    androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                            newFav ? R.color.accent_price : R.color.text_secondary)
            );
            com.example.bookapp.Repository.FavoriteRepository.toggleFavorite(book.getBookId(), currentlyFav);
        });

        holder.itemView.setOnClickListener(v -> listener.onClick(book));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover, ibFavorite;
        TextView tvTitle, tvAuthor, tvRating, tvSalePrice, tvOriginalPrice, tvSaleBadge;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_book_cover);
            ibFavorite = itemView.findViewById(R.id.ib_favorite);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvAuthor = itemView.findViewById(R.id.tv_book_author);
            tvRating = itemView.findViewById(R.id.tv_book_rating);
            tvSalePrice = itemView.findViewById(R.id.tv_book_sale_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_book_original_price);
            tvSaleBadge = itemView.findViewById(R.id.tv_sale_badge);
        }
    }
}
