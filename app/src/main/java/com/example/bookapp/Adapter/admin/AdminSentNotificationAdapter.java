package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Notification;
import com.example.bookapp.R;
import com.example.bookapp.Utils.PriceFormatter;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_notification_sent.xml — dùng cho khối "Đã gửi gần đây" trong ManageNotificationActivity. */
public class AdminSentNotificationAdapter extends RecyclerView.Adapter<AdminSentNotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();

    public void setItems(List<Notification> newItems) {
        this.notifications = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_notification_sent, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification n = notifications.get(position);
        holder.tvTitle.setText(n.getTitle());
        holder.tvContent.setText(n.getContent());
        holder.tvTime.setText(PriceFormatter.formatDate(n.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvContent = itemView.findViewById(R.id.tv_notification_content);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
        }
    }
}
