package com.example.bookapp.Adapter.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookapp.Model.CartItem;
import com.example.bookapp.R;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * CartItem chỉ lưu bookId/quantity/priceAtAdd (xem CartItem.java), nên title/ảnh bìa
 * cần join thêm từ collection "books" - truyền vào qua bookInfoCache giống MyReviewAdapter.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartActionListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemove(CartItem item, int position);
        void onSelectionChanged(CartItem item, boolean selected);
    }

    private final List<CartItem> items;
    private final java.util.Map<String, String[]> bookInfoCache; // bookId -> [title, coverUrl]
    private final Set<String> selectedIds = new HashSet<>();
    private final OnCartActionListener listener;

    public CartAdapter(List<CartItem> items, java.util.Map<String, String[]> bookInfoCache,
                        OnCartActionListener listener) {
        this.items = items;
        this.bookInfoCache = bookInfoCache;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        String[] bookInfo = bookInfoCache.get(item.getBookId());
        if (bookInfo != null) {
            holder.tvTitle.setText(bookInfo[0]);
            Glide.with(holder.itemView.getContext())
                    .load(bookInfo[1])
                    .placeholder(R.drawable.placeholder_book)
                    .into(holder.ivCover);
        }

        holder.tvPrice.setText(currencyFormat.format(item.getSubTotal()) + "đ");
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.cbSelected.setChecked(selectedIds.contains(item.getBookId()));

        holder.cbSelected.setOnClickListener(v -> {
            boolean checked = holder.cbSelected.isChecked();
            if (checked) selectedIds.add(item.getBookId());
            else selectedIds.remove(item.getBookId());
            listener.onSelectionChanged(item, checked);
        });

        holder.ibDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChanged(item, item.getQuantity() - 1);
                notifyItemChanged(holder.getAdapterPosition());
            }
        });
        holder.ibIncrease.setOnClickListener(v -> {
            listener.onQuantityChanged(item, item.getQuantity() + 1);
            notifyItemChanged(holder.getAdapterPosition());
        });
        holder.ibRemove.setOnClickListener(v -> listener.onRemove(item, position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Set<String> getSelectedIds() {
        return selectedIds;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvPrice, tvQuantity;
        CheckBox cbSelected;
        ImageButton ibDecrease, ibIncrease, ibRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cart_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_cart_book_title);
            tvPrice = itemView.findViewById(R.id.tv_cart_book_price);
            tvQuantity = itemView.findViewById(R.id.tv_cart_quantity);
            cbSelected = itemView.findViewById(R.id.cb_item_selected);
            ibDecrease = itemView.findViewById(R.id.ib_cart_decrease);
            ibIncrease = itemView.findViewById(R.id.ib_cart_increase);
            ibRemove = itemView.findViewById(R.id.ib_remove_item);
        }
    }
}
