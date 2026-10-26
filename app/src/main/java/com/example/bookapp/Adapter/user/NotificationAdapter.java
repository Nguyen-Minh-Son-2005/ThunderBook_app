package com.example.bookapp.Adapter.user;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Notification;
import com.example.bookapp.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onClick(Notification notification, int position);
    }

    private final List<Notification> notifications;
    private final OnNotificationClickListener listener;

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvContent.setText(notification.getContent());

        if (notification.getCreatedAt() != null) {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    notification.getCreatedAt().toDate().getTime());
            holder.tvTime.setText(relativeTime);
        }

        // Đổi icon + màu nền theo loại thông báo
        int iconRes;
        switch (notification.getType()) {
            case "order":
                iconRes = R.drawable.ic_order_pending;
                break;
            case "promo":
                iconRes = R.drawable.ic_gift;
                break;
            default:
                iconRes = R.drawable.ic_settings;
        }
        holder.ivTypeIcon.setImageResource(iconRes);

        holder.dotUnread.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);
        holder.itemView.setBackgroundColor(notification.isRead()
                ? holder.itemView.getResources().getColor(R.color.surface)
                : holder.itemView.getResources().getColor(R.color.primary_light));

        holder.itemView.setOnClickListener(v -> listener.onClick(notification, position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTypeIcon;
        TextView tvTitle, tvContent, tvTime;
        View dotUnread;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTypeIcon = itemView.findViewById(R.id.iv_type_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            dotUnread = itemView.findViewById(R.id.dot_unread);
        }
    }
}
