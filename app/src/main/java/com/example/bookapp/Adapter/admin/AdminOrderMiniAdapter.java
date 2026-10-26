package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Order;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.PriceFormatter;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_admin_order_mini.xml — dùng cho khối "Đơn hàng gần đây" trên AdminDashboardActivity. */
public class AdminOrderMiniAdapter extends RecyclerView.Adapter<AdminOrderMiniAdapter.MiniViewHolder> {

    public interface OnOrderMiniClickListener {
        void onOrderClick(Order order);
    }

    private List<Order> orders = new ArrayList<>();
    private final OnOrderMiniClickListener listener;

    public AdminOrderMiniAdapter(OnOrderMiniClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Order> newItems) {
        this.orders = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MiniViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_admin_order_mini, parent, false);
        return new MiniViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MiniViewHolder holder, int position) {
        Order order = orders.get(position);
        android.content.Context ctx = holder.itemView.getContext();

        holder.tvCode.setText("#" + order.getOrderId());
        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        String customerName = order.getShippingAddress() != null ? order.getShippingAddress().getName() : "";
        holder.tvCustomer.setText(customerName + " · " + itemCount + " sản phẩm");
        holder.tvTotal.setText(PriceFormatter.formatVND(order.getFinalTotal()));

        boolean delivered = Constants.ORDER_DELIVERED.equals(order.getOrderStatus());
        holder.tvStatus.setText(delivered ? "Đã giao" : "Chờ xử lý");
        holder.tvStatus.setBackground(ContextCompat.getDrawable(ctx,
                delivered ? R.drawable.bg_pill_success : R.drawable.bg_pill_warning));
        holder.tvStatus.setTextColor(ContextCompat.getColor(ctx,
                delivered ? R.color.success : R.color.warning));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class MiniViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvCustomer, tvTotal, tvStatus;

        MiniViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_order_code);
            tvCustomer = itemView.findViewById(R.id.tv_order_customer);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
        }
    }
}
