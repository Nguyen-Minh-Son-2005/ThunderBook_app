package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_dashboard_menu.xml — lưới 3 cột "Quản lý cửa hàng" trên AdminDashboardActivity. */
public class AdminDashboardMenuAdapter extends RecyclerView.Adapter<AdminDashboardMenuAdapter.MenuViewHolder> {

    public interface OnMenuItemClickListener {
        void onMenuItemClick(AdminDashboardMenuItem item);
    }

    private List<AdminDashboardMenuItem> items = new ArrayList<>();
    private final OnMenuItemClickListener listener;

    public AdminDashboardMenuAdapter(OnMenuItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminDashboardMenuItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_dashboard_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        AdminDashboardMenuItem item = items.get(position);
        holder.ivIcon.setImageResource(item.getIconResId());
        holder.tvLabel.setText(item.getLabel());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMenuItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvLabel;

        MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_menu_icon);
            tvLabel = itemView.findViewById(R.id.tv_menu_label);
        }
    }
}
