package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.OrderItem;
import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;
import com.example.bookapp.Utils.PriceFormatter;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_order_product.xml — danh sách sản phẩm trong AdminOrderDetailActivity. */
public class AdminOrderProductAdapter extends RecyclerView.Adapter<AdminOrderProductAdapter.ProductViewHolder> {

    private List<OrderItem> items = new ArrayList<>();

    public void setItems(List<OrderItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_order_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        OrderItem item = items.get(position);

        // Đọc thẳng từ snapshot lưu trong đơn hàng (title/coverImageUrl/price tại thời
        // điểm mua) — KHÔNG query lại collection books, đúng nguyên tắc đã thống nhất
        // khi thiết kế field "items" trong orders.
        holder.tvTitle.setText(item.getTitle());
        holder.tvQuantity.setText("Số lượng: " + item.getQuantity());
        holder.tvPrice.setText(PriceFormatter.formatVND(item.getPrice() * item.getQuantity()));
        ImageUtils.loadImage(holder.ivCover, item.getCoverImageUrl());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvQuantity, tvPrice;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_item_cover);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvQuantity = itemView.findViewById(R.id.tv_item_quantity);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
        }
    }
}
