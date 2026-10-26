package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Publisher;
import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_admin_simple_entity.xml — dùng cho ManagePublisherActivity. */
public class AdminPublisherAdapter extends RecyclerView.Adapter<AdminPublisherAdapter.PublisherViewHolder> {

    public interface OnPublisherActionListener {
        void onEditClick(Publisher publisher);

        void onDeleteClick(Publisher publisher);
    }

    private List<Publisher> publishers = new ArrayList<>();
    private final OnPublisherActionListener listener;

    public AdminPublisherAdapter(OnPublisherActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Publisher> newItems) {
        this.publishers = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PublisherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_admin_simple_entity, parent, false);
        return new PublisherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublisherViewHolder holder, int position) {
        Publisher publisher = publishers.get(position);

        holder.tvName.setText(publisher.getName());
        holder.tvSubtitle.setVisibility(View.GONE);
        ImageUtils.loadImage(holder.ivAvatar, publisher.getLogoUrl(), R.drawable.ic_publisher);

        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(publisher);
        });
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(publisher);
        });
    }

    @Override
    public int getItemCount() {
        return publishers.size();
    }

    static class PublisherViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivEdit, ivDelete;
        TextView tvName, tvSubtitle;

        PublisherViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_entity_avatar);
            tvName = itemView.findViewById(R.id.tv_entity_name);
            tvSubtitle = itemView.findViewById(R.id.tv_entity_subtitle);
            ivEdit = itemView.findViewById(R.id.iv_entity_edit);
            ivDelete = itemView.findViewById(R.id.iv_entity_delete);
        }
    }
}
