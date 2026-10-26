package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Category;
import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_admin_simple_entity.xml — dùng cho ManageCategoryActivity. */
public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEditClick(Category category);

        void onDeleteClick(Category category);
    }

    private List<Category> categories = new ArrayList<>();
    private final OnCategoryActionListener listener;

    public AdminCategoryAdapter(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Category> newItems) {
        this.categories = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_admin_simple_entity, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.tvName.setText(category.getName());
        holder.tvSubtitle.setText(Boolean.TRUE.equals(category.isActive()) ? "Đang hiển thị" : "Đã ẩn");
        ImageUtils.loadImage(holder.ivAvatar, category.getImageUrl());

        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(category);
        });
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivEdit, ivDelete;
        TextView tvName, tvSubtitle;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_entity_avatar);
            tvName = itemView.findViewById(R.id.tv_entity_name);
            tvSubtitle = itemView.findViewById(R.id.tv_entity_subtitle);
            ivEdit = itemView.findViewById(R.id.iv_entity_edit);
            ivDelete = itemView.findViewById(R.id.iv_entity_delete);
        }
    }
}
