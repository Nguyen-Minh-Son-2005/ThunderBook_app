package com.example.bookapp.View.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.admin.AdminOrderAdapter;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.ViewModel.AdminOrderViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class ManageOrderActivity extends AdminBaseActivity {

    /** Khớp đúng thứ tự 6 Tab trong admin_activity_manage_order.xml — null = Tab "Tất cả". */
    private static final String[] TAB_STATUS = {
            null, Constants.ORDER_PENDING, Constants.ORDER_PACKING,
            Constants.ORDER_SHIPPING, Constants.ORDER_DELIVERED, Constants.ORDER_CANCELLED
    };

    private AdminOrderViewModel viewModel;
    private AdminOrderAdapter adapter;
    private View emptyState;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_manage_order);

        viewModel = new ViewModelProvider(this).get(AdminOrderViewModel.class);

        setupToolbar(findViewById(R.id.tb_toolbar), "Quản lý đơn hàng");
        setupSearch();
        setupTabs();
        setupRecyclerView();

        emptyState = findViewById(R.id.empty_state);

        viewModel.getDisplayedOrders().observe(this, this::onListChanged);
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.setHint("Tìm theo mã đơn / tên khách hàng");
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        FrameLayout flSort = findViewById(R.id.fl_sort);
        flSort.setOnClickListener(v -> showSortMenu(v));
    }

    private void showSortMenu(View anchor) {
        android.widget.PopupMenu menu = new android.widget.PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, "Mới nhất");
        menu.getMenu().add(0, 2, 1, "Cũ nhất");
        menu.getMenu().add(0, 3, 2, "Giá trị cao → thấp");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    viewModel.setSortOption(AdminOrderViewModel.SortOption.NEWEST);
                    break;
                case 2:
                    viewModel.setSortOption(AdminOrderViewModel.SortOption.OLDEST);
                    break;
                case 3:
                    viewModel.setSortOption(AdminOrderViewModel.SortOption.HIGHEST_VALUE);
                    break;
            }
            return true;
        });
        menu.show();
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tl_order_status);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position >= 0 && position < TAB_STATUS.length) {
                    viewModel.setStatusFilter(TAB_STATUS[position]);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_order_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(order -> {
            Intent intent = new Intent(this, AdminOrderDetailActivity.class);
            intent.putExtra(Constants.EXTRA_ORDER_ID, order.getOrderId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }

    private void onListChanged(List<com.example.bookapp.Model.Order> orders) {
        adapter.setItems(orders);
        emptyState.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
