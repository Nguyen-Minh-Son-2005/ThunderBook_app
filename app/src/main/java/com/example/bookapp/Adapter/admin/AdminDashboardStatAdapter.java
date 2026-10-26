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

/** Bind vào admin_item_dashboard_stat.xml — 4 thẻ thống kê ngang trên AdminDashboardActivity. */
public class AdminDashboardStatAdapter extends RecyclerView.Adapter<AdminDashboardStatAdapter.StatViewHolder> {

    private List<AdminDashboardStat> stats = new ArrayList<>();

    public void setItems(List<AdminDashboardStat> newItems) {
        this.stats = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_dashboard_stat, parent, false);
        return new StatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatViewHolder holder, int position) {
        AdminDashboardStat stat = stats.get(position);
        holder.ivIcon.setImageResource(stat.getIconResId());
        holder.tvValue.setText(stat.getValue());
        holder.tvLabel.setText(stat.getLabel());
        if (stat.getGrowthLabel() != null && !stat.getGrowthLabel().isEmpty()) {
            holder.tvGrowth.setVisibility(View.VISIBLE);
            holder.tvGrowth.setText(stat.getGrowthLabel());
        } else {
            holder.tvGrowth.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }

    static class StatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvValue, tvLabel, tvGrowth;

        StatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_stat_icon);
            tvValue = itemView.findViewById(R.id.tv_stat_value);
            tvLabel = itemView.findViewById(R.id.tv_stat_label);
            tvGrowth = itemView.findViewById(R.id.tv_stat_growth);
        }
    }
}
