package com.example.bookapp.View.user;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookapp.Adapter.user.NotificationAdapter;
import com.example.bookapp.Model.Notification;
import com.example.bookapp.R;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.ViewModel.NotificationViewModel;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class NotificationActivity extends AppCompatActivity {

    private SwipeRefreshLayout srl;
    private RecyclerView rvNotifications;
    private LinearLayout llEmpty;

    private NotificationAdapter adapter;
    private final List<Notification> notificationList = new ArrayList<>();

    private NotificationViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        srl = findViewById(R.id.srl_notifications);
        rvNotifications = findViewById(R.id.rv_notifications);
        llEmpty = findViewById(R.id.ll_empty);

        rvNotifications.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList, (notification, position) -> {
            if (!notification.isRead()) {
                viewModel.markAsRead(notification);
            }
            if (com.example.bookapp.Utils.Constants.NOTIF_TYPE_ORDER.equals(notification.getType())
                    && notification.getRelatedId() != null) {
                Intent intent = new Intent(this, OrderDetailActivity.class);
                intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, notification.getRelatedId());
                startActivity(intent);
            }
        });
        rvNotifications.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        viewModel.getNotifications().observe(this, notifications -> {
            srl.setRefreshing(false);
            notificationList.clear();
            if (notifications != null) notificationList.addAll(notifications);
            adapter.notifyDataSetChanged();

            boolean isEmpty = notificationList.isEmpty();
            llEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvNotifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) srl.setRefreshing(true);
        });

        findViewById(R.id.tv_mark_all_read).setOnClickListener(v ->
                viewModel.markAllAsRead(new ArrayList<>(notificationList)));

        srl.setOnRefreshListener(() -> {
            String uid = FirebaseUtils.getCurrentUserId();
            if (uid != null) viewModel.loadNotifications(uid);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid != null) viewModel.loadNotifications(uid);
    }
}
