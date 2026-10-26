package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Book;
import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;
import com.example.bookapp.Utils.PriceFormatter;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_admin_book.xml — dùng cho ManageBookActivity. */
public class AdminBookAdapter extends RecyclerView.Adapter<AdminBookAdapter.BookViewHolder> {

    /** Tách riêng từng loại thao tác thay vì 1 interface onClick duy nhất — item này có
     * nhiều vùng bấm khác nhau (cả item, nút menu 3 chấm, switch bật/tắt). */
    public interface OnBookActionListener {
        void onItemClick(Book book);

        void onMenuClick(Book book, View anchorView);

        void onToggleActive(Book book, boolean newActiveState);
    }

    private List<Book> books = new ArrayList<>();
    private final OnBookActionListener listener;

    public AdminBookAdapter(OnBookActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Book> newItems) {
        this.books = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_admin_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);

        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthorNameDisplay());
        ImageUtils.loadImage(holder.ivCover, book.getCoverImageUrl());

        boolean onSale = PriceFormatter.isOnSale(book.getPrice(), book.getSalePrice());
        holder.tvPrice.setText(PriceFormatter.formatVND(onSale ? book.getSalePrice() : book.getPrice()));
        if (onSale) {
            holder.tvPriceOld.setVisibility(View.VISIBLE);
            holder.tvPriceOld.setText(PriceFormatter.formatVND(book.getPrice()));
            holder.tvPriceOld.setPaintFlags(holder.tvPriceOld.getPaintFlags()
                    | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvPriceOld.setVisibility(View.GONE);
        }

        holder.tvStock.setText("Còn " + book.getStock());
        holder.tvSold.setText("Đã bán " + book.getSoldCount());

        // Gỡ listener cũ trước khi set lại giá trị, tránh switch tự bắn callback khi
        // RecyclerView tái sử dụng view (recycle) — lỗi rất hay gặp với SwitchCompat trong Adapter.
        holder.swActive.setOnCheckedChangeListener(null);
        holder.swActive.setChecked(book.isActive());
        holder.swActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggleActive(book, isChecked);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(book);
        });
        holder.ivMenu.setOnClickListener(v -> {
            if (listener != null) listener.onMenuClick(book, v);
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover, ivMenu;
        TextView tvTitle, tvAuthor, tvPrice, tvPriceOld, tvStock, tvSold;
        SwitchCompat swActive;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvAuthor = itemView.findViewById(R.id.tv_book_author);
            tvPrice = itemView.findViewById(R.id.tv_book_price);
            tvPriceOld = itemView.findViewById(R.id.tv_book_price_old);
            tvStock = itemView.findViewById(R.id.tv_book_stock);
            tvSold = itemView.findViewById(R.id.tv_book_sold);
            ivMenu = itemView.findViewById(R.id.iv_book_menu);
            swActive = itemView.findViewById(R.id.sw_book_active);
        }
    }
}

/*
 * GHI CHÚ: đang hiển thị tạm book.getAuthorIds().toString() (in thẳng mảng id thô, kiểu
 * "[author_001, author_045]") — chấp nhận được để build/test trước, nhưng KHÔNG nên để
 * vậy khi nộp bài vì không thân thiện người dùng. Khi viết AdminBookViewModel, join tên
 * tác giả thật từ danh sách Author đã tải (AdminAuthorRepository) rồi map lại List<Book>
 * trước khi đưa cho Adapter, hoặc đơn giản hơn là bỏ hẳn dòng hiển thị tác giả trong danh
 * sách rút gọn — chỉ hiện đầy đủ tên tác giả ở AddEditBookActivity (nơi vốn đã cần load
 * danh sách Author cho dropdown chọn tác giả).
 */
