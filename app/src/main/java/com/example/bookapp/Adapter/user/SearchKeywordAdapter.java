package com.example.bookapp.Adapter.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;

import java.util.List;

/**
 * Dùng chung cho gợi ý tìm kiếm gần đây (rv_recent_searches) và
 * từ khóa phổ biến (rv_popular_keywords) trong activity_search.xml.
 */
public class SearchKeywordAdapter extends RecyclerView.Adapter<SearchKeywordAdapter.KeywordViewHolder> {

    public interface OnKeywordClickListener {
        void onClick(String keyword);
    }

    private final List<String> keywords;
    private final OnKeywordClickListener listener;

    public SearchKeywordAdapter(List<String> keywords, OnKeywordClickListener listener) {
        this.keywords = keywords;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_keyword, parent, false);
        return new KeywordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeywordViewHolder holder, int position) {
        String keyword = keywords.get(position);
        holder.tvKeyword.setText(keyword);
        holder.itemView.setOnClickListener(v -> listener.onClick(keyword));
    }

    @Override
    public int getItemCount() {
        return keywords.size();
    }

    static class KeywordViewHolder extends RecyclerView.ViewHolder {
        TextView tvKeyword;

        KeywordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKeyword = itemView.findViewById(R.id.tv_keyword);
        }
    }
}
