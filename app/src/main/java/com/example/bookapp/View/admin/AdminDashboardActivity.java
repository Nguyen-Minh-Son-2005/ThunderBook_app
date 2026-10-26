package com.example.bookapp.View.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.admin.AdminDashboardMenuItem;
import com.example.bookapp.Adapter.admin.AdminDashboardMenuAdapter;
import com.example.bookapp.Adapter.admin.AdminDashboardStatAdapter;
import com.example.bookapp.Adapter.admin.AdminOrderMiniAdapter;
import com.example.bookapp.Adapter.admin.AdminTopBookAdapter;
import com.example.bookapp.R;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.Utils.ImageUtils;
import com.example.bookapp.Utils.SessionManager;
import com.example.bookapp.ViewModel.AdminDashboardViewModel;
import com.example.bookapp.View.user.LoginActivity;
import com.google.android.material.chip.ChipGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import androidx.core.content.ContextCompat;

public class AdminDashboardActivity extends AdminBaseActivity {

    private AdminDashboardViewModel viewModel;
    private TextView tvRevenueTotal;
    private BarChart barChart;

    private AdminDashboardStatAdapter statAdapter;
    private AdminDashboardMenuAdapter menuAdapter;
    private AdminOrderMiniAdapter recentOrderAdapter;
    private AdminTopBookAdapter topBookAdapter;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_admin_dashboard);

        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        setupHeader();
        setupChartSection();
        setupRecyclerViews();
        observeViewModel();

        viewModel.loadStats();
        // Mặc định load doanh thu tuần cho bảng
        viewModel.loadChartRevenue(AdminDashboardViewModel.Period.WEEK);
    }

    private void setupHeader() {
        ImageView ivAvatar = findViewById(R.id.iv_detail_avatar);
        TextView tvName = findViewById(R.id.tv_admin_name);
        SessionManager session = new SessionManager(this);
        tvName.setText(session.getUserName());
        ImageUtils.loadImage(ivAvatar, session.getUserAvatar(), R.drawable.ic_user);

        findViewById(R.id.fl_notification).setOnClickListener(v -> {
            // TODO: mở NotificationActivity dùng chung với User khi có, hoặc
            // tạo màn thông báo riêng cho Admin nếu nghiệp vụ khác nhau đủ nhiều.
        });

        findViewById(R.id.fl_logout).setOnClickListener(v -> logout());

        findViewById(R.id.tv_view_all_orders).setOnClickListener(v ->
                startActivity(new Intent(this, ManageOrderActivity.class)));

        android.widget.Button btnSync = findViewById(R.id.btn_sync_sold_count);
        if (btnSync != null) {
            btnSync.setOnClickListener(v -> {
                btnSync.setEnabled(false);
                btnSync.setText("Đang Đồng Bộ...");
                viewModel.syncHistoricalSoldCount(new com.example.bookapp.Utils.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        android.widget.Toast.makeText(AdminDashboardActivity.this, "Đồng bộ thành công!", android.widget.Toast.LENGTH_SHORT).show();
                        btnSync.setText("Đã Đồng Bộ Xong");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        android.widget.Toast.makeText(AdminDashboardActivity.this, "Lỗi: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        btnSync.setEnabled(true);
                        btnSync.setText("Thử Lại");
                    }
                });
            });
        }
    }

    private void logout() {
        FirebaseUtils.signOut();
        new SessionManager(this).clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupChartSection() {
        tvRevenueTotal = findViewById(R.id.tv_revenue_total);
        barChart = findViewById(R.id.chart_revenue);
        setupBarChart();
        
        ChipGroup chipGroup = findViewById(R.id.cg_revenue_filter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_week) {
                viewModel.loadChartRevenue(AdminDashboardViewModel.Period.WEEK);
            } else if (id == R.id.chip_month) {
                viewModel.loadChartRevenue(AdminDashboardViewModel.Period.MONTH);
            } else if (id == R.id.chip_year) {
                viewModel.loadChartRevenue(AdminDashboardViewModel.Period.YEAR);
            }
        });
    }

    private void setupBarChart() {
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);
        barChart.setExtraBottomOffset(10f); // Thêm khoảng trống dưới X-Axis để text không bị cắt

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisLineColor(ContextCompat.getColor(this, R.color.divider));
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(ContextCompat.getColor(this, R.color.divider));
        barChart.getAxisLeft().setAxisLineColor(ContextCompat.getColor(this, android.R.color.transparent));
        barChart.getAxisLeft().setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);
    }

    private void setupRecyclerViews() {
        RecyclerView rvStats = findViewById(R.id.rv_stat_cards);
        statAdapter = new AdminDashboardStatAdapter();
        rvStats.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvStats.setAdapter(statAdapter);

        RecyclerView rvMenu = findViewById(R.id.rv_management_grid);
        menuAdapter = new AdminDashboardMenuAdapter(this::openMenuTarget);
        rvMenu.setLayoutManager(new GridLayoutManager(this, 3));
        rvMenu.setAdapter(menuAdapter);
        menuAdapter.setItems(viewModel.getMenuItems());

        RecyclerView rvRecentOrders = findViewById(R.id.rv_recent_orders);
        recentOrderAdapter = new AdminOrderMiniAdapter(order -> {
            Intent intent = new Intent(this, AdminOrderDetailActivity.class);
            intent.putExtra(com.example.bookapp.Utils.Constants.EXTRA_ORDER_ID, order.getOrderId());
            startActivity(intent);
        });
        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        rvRecentOrders.setAdapter(recentOrderAdapter);

        RecyclerView rvTopBooks = findViewById(R.id.rv_top_books);
        topBookAdapter = new AdminTopBookAdapter(book -> {
            Intent intent = new Intent(this, AddEditBookActivity.class);
            intent.putExtra(com.example.bookapp.Utils.Constants.EXTRA_BOOK_ID, book.getBookId());
            intent.putExtra(com.example.bookapp.Utils.Constants.EXTRA_MODE_EDIT, true);
            startActivity(intent);
        });
        rvTopBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTopBooks.setAdapter(topBookAdapter);
    }

    private void observeViewModel() {
        viewModel.getStatCards().observe(this, statAdapter::setItems);
        viewModel.getRecentOrders().observe(this, recentOrderAdapter::setItems);
        viewModel.getTopSellingBooks().observe(this, topBookAdapter::setItems);
        viewModel.getChartRevenue().observe(this, result -> {
            if (result != null) {
                tvRevenueTotal.setText(com.example.bookapp.Utils.PriceFormatter.formatVND(result.getTotalRevenue()));
                
                BarDataSet dataSet = new BarDataSet(result.getEntries(), "Doanh thu");
                int startColor = ContextCompat.getColor(this, R.color.primary_light);
                int endColor = ContextCompat.getColor(this, R.color.primary);
                dataSet.setGradientColor(startColor, endColor);
                dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                dataSet.setValueTextSize(10f);
                
                dataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if (value == 0) return "";
                        if (value >= 1000000) return String.format(java.util.Locale.US, "%.1fM", value / 1000000);
                        if (value >= 1000) return String.format(java.util.Locale.US, "%.0fK", value / 1000);
                        return String.valueOf((int) value);
                    }
                });

                BarData data = new BarData(dataSet);
                data.setBarWidth(0.6f);
                
                barChart.setData(data);
                barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(result.getLabels()));
                
                barChart.animateY(800);
                barChart.invalidate();
            } else {
                tvRevenueTotal.setText("...");
                barChart.clear();
            }
        });
    }

    /** Điều hướng theo targetKey của AdminDashboardMenuItem (xem AdminDashboardViewModel.getMenuItems()). */
    private void openMenuTarget(com.example.bookapp.Adapter.admin.AdminDashboardMenuItem item) {
        Intent intent;
        switch (item.getTargetKey()) {
            case "manage_book":
                intent = new Intent(this, ManageBookActivity.class);
                break;
            case "manage_category":
                intent = new Intent(this, ManageCategoryActivity.class);
                break;
            case "manage_author":
                intent = new Intent(this, ManageAuthorActivity.class);
                break;
            case "manage_publisher":
                intent = new Intent(this, ManagePublisherActivity.class);
                break;
            case "manage_order":
                intent = new Intent(this, ManageOrderActivity.class);
                break;
            case "manage_user":
                intent = new Intent(this, ManageUserActivity.class);
                break;
            case "manage_voucher":
                intent = new Intent(this, ManageVoucherActivity.class);
                break;
            case "manage_notification":
                intent = new Intent(this, ManageNotificationActivity.class);
                break;
            case "statistic":
                intent = new Intent(this, StatisticActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
    }
}
