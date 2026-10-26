package com.example.bookapp.Adapter.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookapp.Model.Category;
import com.example.bookapp.R;

import java.util.List;

public class SearchCategoryAdapter extends RecyclerView.Adapter<SearchCategoryAdapter.SearchCategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onClick(Category category);
    }

    private final List<Category> categories;
    private final OnCategoryClickListener listener;

    public SearchCategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_category, parent, false);
        return new SearchCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchCategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvName.setText(category.getName());
        // Số lượng sách trong danh mục cần đếm riêng (vd query where categoryIds
        // array-contains categoryId) - tạm để trống, gắn count() khi wiring thật.
        holder.tvCount.setText("");
        Glide.with(holder.itemView.getContext())
                .load(category.getImageUrl())
                .placeholder(R.drawable.ic_category)
                .into(holder.ivIcon);
        holder.itemView.setOnClickListener(v -> listener.onClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class SearchCategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvCount;

        SearchCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_search_category_icon);
            tvName = itemView.findViewById(R.id.tv_search_category_name);
            tvCount = itemView.findViewById(R.id.tv_search_category_count);
        }
    }
}
