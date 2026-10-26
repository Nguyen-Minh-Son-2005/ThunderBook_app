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

import java.util.List;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.ProductViewHolder> {

    private final List<OrderItem> items;

    public OrderProductAdapter(List<OrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvQty.setText("x" + item.getQuantity());
        Glide.with(holder.itemView.getContext())
                .load(item.getCoverImageUrl())
                .into(holder.ivThumb);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvQty;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_product_thumb);
            tvTitle = itemView.findViewById(R.id.tv_product_title);
            tvQty = itemView.findViewById(R.id.tv_product_qty);
        }
    }
}
