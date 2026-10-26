package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Bind vào admin_item_stat_rank_row.xml — dùng CHUNG cho cả 2 RecyclerView trong
 * StatisticActivity (rv_stat_top_books và rv_stat_top_customers), mỗi cái tự tạo 1
 * instance Adapter riêng với dữ liệu List<AdminRankItem> đã map sẵn từ ViewModel.
 */
public class AdminStatRankAdapter extends RecyclerView.Adapter<AdminStatRankAdapter.RankViewHolder> {

    private List<AdminRankItem> items = new ArrayList<>();

    public void setItems(List<AdminRankItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_stat_rank_row, parent, false);
        return new RankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankViewHolder holder, int position) {
        AdminRankItem item = items.get(position);
        holder.tvRankNumber.setText(String.valueOf(position + 1));
        holder.tvName.setText(item.getName());
        holder.tvValue.setText(item.getValueLabel());
        ImageUtils.loadImage(holder.ivAvatar, item.getAvatarUrl());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RankViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvRankNumber, tvName, tvValue;

        RankViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRankNumber = itemView.findViewById(R.id.tv_rank_number);
            ivAvatar = itemView.findViewById(R.id.iv_rank_avatar);
            tvName = itemView.findViewById(R.id.tv_rank_name);
            tvValue = itemView.findViewById(R.id.tv_rank_value);
        }
    }
}
