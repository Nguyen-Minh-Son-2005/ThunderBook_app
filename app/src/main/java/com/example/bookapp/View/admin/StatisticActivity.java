package com.example.bookapp.View.admin;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.admin.AdminStatRankAdapter;
import com.example.bookapp.R;
import com.example.bookapp.Utils.PriceFormatter;
import com.example.bookapp.ViewModel.AdminStatisticViewModel;
import com.google.android.material.chip.ChipGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import androidx.core.content.ContextCompat;

public class StatisticActivity extends AdminBaseActivity {

    private AdminStatisticViewModel viewModel;
    private AdminStatRankAdapter topBooksAdapter;
    private AdminStatRankAdapter topCustomersAdapter;

    private android.widget.TextView tvRevenue, tvOrderCount, tvAov;
    private LineChart lineChart;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_statistic);

        viewModel = new ViewModelProvider(this).get(AdminStatisticViewModel.class);

        setupToolbar(findViewById(R.id.tb_toolbar), "Thống kê chi tiết");
        bindSummaryViews();
        setupLineChart();
        setupPeriodChips();
        setupRecyclerViews();
        observeViewModel();

        // Doanh thu/số đơn/TB theo kỳ mặc định (Tuần, đã checked=true sẵn trong layout).
        viewModel.loadRevenue(AdminStatisticViewModel.Period.WEEK);
        // Top sách/khách hàng là số liệu TOÀN THỜI GIAN, không phụ thuộc chip kỳ — chỉ tải 1 lần.
        viewModel.loadTopBooks(10);
        viewModel.loadTopCustomers(10);
    }

    private void bindSummaryViews() {
        tvRevenue = findViewById(R.id.tv_stat_revenue_value);
        tvOrderCount = findViewById(R.id.tv_stat_order_value);
        tvAov = findViewById(R.id.tv_stat_aov_value);
    }

    private void setupLineChart() {
        lineChart = findViewById(R.id.chart_statistic_revenue);
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setExtraBottomOffset(10f);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisLineColor(ContextCompat.getColor(this, R.color.divider));
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setGridColor(ContextCompat.getColor(this, R.color.divider));
        lineChart.getAxisLeft().setAxisLineColor(ContextCompat.getColor(this, android.R.color.transparent));
        lineChart.getAxisLeft().setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisRight().setEnabled(false);
    }

    private void setupPeriodChips() {
        ChipGroup chipGroup = findViewById(R.id.cg_stat_period);
        chipGroup.setSingleSelection(true);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            AdminStatisticViewModel.Period period;
            if (id == R.id.chip_stat_day) {
                period = AdminStatisticViewModel.Period.DAY;
            } else if (id == R.id.chip_stat_month) {
                period = AdminStatisticViewModel.Period.MONTH;
            } else if (id == R.id.chip_stat_year) {
                period = AdminStatisticViewModel.Period.YEAR;
            } else {
                period = AdminStatisticViewModel.Period.WEEK;
            }
            viewModel.loadRevenue(period);
        });
    }

    private void setupRecyclerViews() {
        RecyclerView rvTopBooks = findViewById(R.id.rv_stat_top_books);
        rvTopBooks.setLayoutManager(new LinearLayoutManager(this));
        topBooksAdapter = new AdminStatRankAdapter();
        rvTopBooks.setAdapter(topBooksAdapter);

        RecyclerView rvTopCustomers = findViewById(R.id.rv_stat_top_customers);
        rvTopCustomers.setLayoutManager(new LinearLayoutManager(this));
        topCustomersAdapter = new AdminStatRankAdapter();
        rvTopCustomers.setAdapter(topCustomersAdapter);
    }

    private void observeViewModel() {
        viewModel.getRevenue().observe(this, revenue -> {
            if (revenue != null) tvRevenue.setText(PriceFormatter.formatVND(revenue));
        });
        viewModel.getOrderCount().observe(this, count -> {
            if (count != null) tvOrderCount.setText(String.valueOf(count));
        });
        viewModel.getAverageOrderValue().observe(this, aov -> {
            if (aov != null) tvAov.setText(PriceFormatter.formatVND(aov));
        });
        viewModel.getTopBooks().observe(this, topBooksAdapter::setItems);
        viewModel.getTopCustomers().observe(this, topCustomersAdapter::setItems);

        viewModel.getChartRevenue().observe(this, result -> {
            if (result != null && !result.getEntries().isEmpty()) {
                LineDataSet dataSet = new LineDataSet(result.getEntries(), "Doanh thu");
                
                int color = ContextCompat.getColor(this, R.color.primary);
                dataSet.setColor(color);
                dataSet.setCircleColor(color);
                dataSet.setLineWidth(2f);
                dataSet.setCircleRadius(4f);
                dataSet.setDrawCircleHole(true);
                dataSet.setDrawFilled(true);
                
                // Gradient fill under the line
                dataSet.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.bg_gradient_header));
                
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

                LineData data = new LineData(dataSet);
                lineChart.setData(data);
                lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(result.getLabels()));
                
                lineChart.animateX(800);
                lineChart.invalidate();
            } else {
                lineChart.clear();
            }
        });
    }
}
