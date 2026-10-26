package com.example.bookapp.Adapter.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookapp.Model.OrderItem;
import com.example.bookapp.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Dùng chung cho rv_checkout_items (activity_checkout.xml) và
 * rv_order_items (activity_order_detail.xml) - tái sử dụng layout item_checkout_book.xml.
 */
public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private final List<OrderItem> items;

    public OrderItemAdapter(List<OrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_book, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = items.get(position);
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        holder.tvTitle.setText(item.getTitle());
        holder.tvQtyPrice.setText("x" + item.getQuantity());
        holder.tvPrice.setText(currencyFormat.format(item.getLineTotal()) + "đ");

        Glide.with(holder.itemView.getContext())
                .load(item.getCoverImageUrl())
                .placeholder(R.drawable.placeholder_book)
                .into(holder.ivCover);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvQtyPrice, tvPrice;

        OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_checkout_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_checkout_book_title);
            tvQtyPrice = itemView.findViewById(R.id.tv_checkout_book_qty_price);
            tvPrice = itemView.findViewById(R.id.tv_checkout_book_price);
        }
    }
}
