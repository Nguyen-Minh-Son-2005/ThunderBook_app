package com.example.bookapp.View.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookapp.Adapter.user.OrderAdapter;
import com.example.bookapp.Model.Order;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.ViewModel.OrderHistoryViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_STATUS = "extra_initial_status";

    // Thứ tự phải khớp với thứ tự tab tự thêm ở setupTabs() bên dưới.
    private static final String[] TAB_STATUSES = {
            null, // "Tất cả"
            Constants.ORDER_PENDING,
            Constants.ORDER_SHIPPING,
            Constants.ORDER_DELIVERED,
            Constants.ORDER_CANCELLED
    };

    private RecyclerView rvOrders;
    private SwipeRefreshLayout srlOrders;
    private LinearLayout llEmptyOrders;
    private TabLayout tabOrderStatus;

    private OrderAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();

    private OrderHistoryViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        bindViews();
        setupRecyclerView();
        setupTabs();

        viewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);

        viewModel.getOrders().observe(this, orders -> {
            srlOrders.setRefreshing(false);
            orderList.clear();
            if (orders != null) orderList.addAll(orders);
            adapter.notifyDataSetChanged();

            boolean isEmpty = orderList.isEmpty();
            llEmptyOrders.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) srlOrders.setRefreshing(true);
        });

        String uid = FirebaseUtils.getCurrentUserId();
        String initialStatus = getIntent().getStringExtra(EXTRA_INITIAL_STATUS);
        selectTabForStatus(initialStatus);
        if (uid != null) viewModel.loadOrders(uid, initialStatus);

        srlOrders.setOnRefreshListener(() -> {
            if (uid != null) viewModel.loadOrders(uid, currentSelectedStatus());
        });
    }

    private void bindViews() {
        rvOrders = findViewById(R.id.rv_orders);
        srlOrders = findViewById(R.id.srl_orders);
        llEmptyOrders = findViewById(R.id.ll_empty_orders);
        tabOrderStatus = findViewById(R.id.tab_order_status);
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList, new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onClick(Order order) {
                Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
                intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getOrderId());
                startActivity(intent);
            }

            @Override
            public void onCancelOrder(Order order) {
                new AlertDialog.Builder(OrderHistoryActivity.this)
                        .setTitle("Hủy đơn hàng")
                        .setMessage("Bạn có xác nhận hủy đơn hàng này?")
                        .setPositiveButton("Đồng ý", (dialog, which) -> {
                            viewModel.cancelOrder(order.getOrderId(), new FirebaseCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    Toast.makeText(OrderHistoryActivity.this, "Hủy đơn thành công", Toast.LENGTH_SHORT).show();
                                    String uid = FirebaseUtils.getCurrentUserId();
                                    if (uid != null) viewModel.loadOrders(uid, currentSelectedStatus());
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(OrderHistoryActivity.this, "Không thể hủy đơn hàng", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Đóng", null)
                        .show();
            }

            @Override
            public void onReviewOrder(Order order) {
                if (order.getItems() == null || order.getItems().isEmpty()) return;
                Intent intent = new Intent(OrderHistoryActivity.this, WriteReviewActivity.class);
                intent.putExtra(WriteReviewActivity.EXTRA_ORDER_ID, order.getOrderId());
                intent.putExtra(WriteReviewActivity.EXTRA_BOOK_ID, order.getItems().get(0).getBookId());
                startActivity(intent);
            }
        });
        rvOrders.setAdapter(adapter);
    }

    private void setupTabs() {
        tabOrderStatus.removeAllTabs();
        tabOrderStatus.addTab(tabOrderStatus.newTab().setText("Tất cả"));
        tabOrderStatus.addTab(tabOrderStatus.newTab().setText("Chờ xác nhận"));
        tabOrderStatus.addTab(tabOrderStatus.newTab().setText("Đang giao"));
        tabOrderStatus.addTab(tabOrderStatus.newTab().setText("Đã giao"));
        tabOrderStatus.addTab(tabOrderStatus.newTab().setText("Đã hủy"));

        tabOrderStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String uid = FirebaseUtils.getCurrentUserId();
                if (uid != null) viewModel.loadOrders(uid, TAB_STATUSES[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void selectTabForStatus(@Nullable String status) {
        for (int i = 0; i < TAB_STATUSES.length; i++) {
            if ((status == null && TAB_STATUSES[i] == null) ||
                    (status != null && status.equals(TAB_STATUSES[i]))) {
                TabLayout.Tab tab = tabOrderStatus.getTabAt(i);
                if (tab != null) tab.select();
                break;
            }
        }
    }

    @Nullable
    private String currentSelectedStatus() {
        return TAB_STATUSES[tabOrderStatus.getSelectedTabPosition()];
    }
}
