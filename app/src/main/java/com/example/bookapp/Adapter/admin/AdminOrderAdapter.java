package com.example.bookapp.Adapter.admin;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Order;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.ImageUtils;
import com.example.bookapp.Utils.PriceFormatter;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_admin_order.xml — dùng cho ManageOrderActivity. */
public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private List<Order> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public AdminOrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Order> newItems) {
        this.orders = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.tvOrderId.setText("#" + order.getOrderId());
        holder.tvOrderDate.setText(PriceFormatter.formatDateTime(order.getCreatedAt().toDate()));

        if (order.getShippingAddress() != null) {
            holder.tvCustomerName.setText(order.getShippingAddress().getName());
        }
        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        holder.tvItemCount.setText(itemCount + " sản phẩm");
        holder.tvTotal.setText(PriceFormatter.formatVND(order.getFinalTotal()));

        bindStatusPill(holder, order.getOrderStatus());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    /** Đổi màu + label pill trạng thái tương ứng — dùng lại bg_pill_* đã có sẵn trong drawable. */
    private void bindStatusPill(OrderViewHolder holder, String status) {
        android.content.Context ctx = holder.itemView.getContext();
        String label;
        int bgRes;
        int textColorRes;

        if (Constants.ORDER_DELIVERED.equals(status)) {
            label = "Đã giao";
            bgRes = R.drawable.bg_pill_success;
            textColorRes = R.color.success;
        } else if (Constants.ORDER_CANCELLED.equals(status) || Constants.ORDER_RETURNED.equals(status)) {
            label = Constants.ORDER_RETURNED.equals(status) ? "Đã trả hàng" : "Đã hủy";
            bgRes = R.drawable.bg_pill_error;
            textColorRes = R.color.error;
        } else if (Constants.ORDER_SHIPPING.equals(status)) {
            label = "Đang giao";
            bgRes = R.drawable.bg_pill_primary;
            textColorRes = R.color.primary;
        } else if (Constants.ORDER_PACKING.equals(status)) {
            label = "Đang đóng gói";
            bgRes = R.drawable.bg_pill_warning;
            textColorRes = R.color.warning;
        } else if (Constants.ORDER_CONFIRMED.equals(status)) {
            label = "Đã xác nhận";
            bgRes = R.drawable.bg_pill_primary;
            textColorRes = R.color.primary;
        } else {
            label = "Chờ xử lý";
            bgRes = R.drawable.bg_pill_warning;
            textColorRes = R.color.warning;
        }

        holder.tvStatus.setText(label);
        holder.tvStatus.setBackground(ContextCompat.getDrawable(ctx, bgRes));
        holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, textColorRes));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCustomerAvatar;
        TextView tvOrderId, tvOrderDate, tvCustomerName, tvItemCount, tvTotal, tvStatus, tvDetailLink;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            ivCustomerAvatar = itemView.findViewById(R.id.iv_customer_avatar);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvItemCount = itemView.findViewById(R.id.tv_order_item_count);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvDetailLink = itemView.findViewById(R.id.tv_order_detail_link);
        }
    }
}
