package com.example.bookapp.Adapter.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Order;
import com.example.bookapp.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onClick(Order order);
        void onCancelOrder(Order order);
        void onReviewOrder(Order order);
    }

    private final List<Order> orders;
    private final OnOrderClickListener listener;

    public OrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        holder.tvStatus.setText(mapStatusToLabel(order.getOrderStatus()));
        
        // Show all items using OrderProductAdapter
        if (order.getItems() != null) {
            OrderProductAdapter productAdapter = new OrderProductAdapter(order.getItems());
            holder.rvItems.setAdapter(productAdapter);
        }

        holder.tvItemCount.setText(order.getTotalItemCount() + " sản phẩm");
        holder.tvFinalTotal.setText(currencyFormat.format(order.getFinalTotal()) + "đ");

        boolean canCancel = "pending".equals(order.getOrderStatus());
        boolean canReview = "delivered".equals(order.getOrderStatus());

        if (canCancel) {
            holder.btnSecondary.setVisibility(View.VISIBLE);
            holder.btnSecondary.setText("Hủy");
            holder.btnSecondary.setTextColor(android.graphics.Color.parseColor("#F44336"));
            holder.btnSecondary.setBackgroundResource(R.drawable.bg_button_outline_red);
        } else if (canReview) {
            holder.btnSecondary.setVisibility(View.VISIBLE);
            holder.btnSecondary.setText("Đánh giá");
            holder.btnSecondary.setTextColor(android.graphics.Color.WHITE);
            holder.btnSecondary.setBackgroundResource(R.drawable.bg_button_primary);
        } else {
            holder.btnSecondary.setVisibility(View.GONE);
        }
        holder.btnPrimary.setText("Xem chi tiết");

        holder.itemView.setOnClickListener(v -> listener.onClick(order));
        holder.btnPrimary.setOnClickListener(v -> listener.onClick(order));

        holder.btnSecondary.setOnClickListener(v -> {
            if (canCancel) listener.onCancelOrder(order);
            else if (canReview) listener.onReviewOrder(order);
        });
    }

    private String mapStatusToLabel(String status) {
        if (status == null) return "";
        switch (status) {
            case "pending": return "Chờ xác nhận";
            case "confirmed": return "Đã xác nhận";
            case "packing": return "Đang đóng gói";
            case "shipping": return "Đang giao";
            case "delivered": return "Đã giao";
            case "cancelled": return "Đã hủy";
            case "returned": return "Trả hàng";
            default: return status;
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvItemCount, tvFinalTotal;
        Button btnPrimary, btnSecondary;
        RecyclerView rvItems;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvItemCount = itemView.findViewById(R.id.tv_order_item_count);
            tvFinalTotal = itemView.findViewById(R.id.tv_order_final_total);
            btnPrimary = itemView.findViewById(R.id.btn_order_action_primary);
            btnSecondary = itemView.findViewById(R.id.btn_order_action_secondary);
            rvItems = itemView.findViewById(R.id.rv_order_items);
        }
    }
}
