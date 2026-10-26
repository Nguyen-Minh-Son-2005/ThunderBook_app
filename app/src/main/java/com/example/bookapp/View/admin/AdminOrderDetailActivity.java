package com.example.bookapp.View.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.admin.AdminOrderProductAdapter;
import com.example.bookapp.Model.Order;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.PriceFormatter;
import com.example.bookapp.ViewModel.AdminOrderDetailViewModel;

public class AdminOrderDetailActivity extends AdminBaseActivity {

    private AdminOrderDetailViewModel viewModel;
    private AdminOrderProductAdapter productAdapter;

    private String orderId;
    private Order currentOrder;

    private TextView tvCurrentStatus, tvReceiverName, tvReceiverAddress, tvOrderNote;
    private android.widget.LinearLayout llTimeline;
    private com.google.android.material.button.MaterialButton btnUpdateStatus, btnCancelOrder;
    private TextView tvSubtotal, tvShippingFee, tvDiscount, tvFinalTotal;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_admin_order_detail);

        orderId = getIntent().getStringExtra(Constants.EXTRA_ORDER_ID);
        if (orderId == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(AdminOrderDetailViewModel.class);

        bindViews();
        setupToolbar(findViewById(R.id.tb_toolbar), "Chi tiết đơn #" + orderId);
        setupRecyclerView();

        viewModel.loadOrder(orderId).observe(this, this::onOrderLoaded);
        viewModel.getErrorMessage().observe(this, this::showError);
        viewModel.getUpdateSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                android.widget.Toast.makeText(this, "Đã cập nhật đơn hàng", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdateStatus.setOnClickListener(v -> handleUpdateStatus());
        btnCancelOrder.setOnClickListener(v -> confirmCancel());
    }

    private void bindViews() {
        tvCurrentStatus = findViewById(R.id.tv_current_status);
        tvReceiverName = findViewById(R.id.tv_receiver_name);
        tvReceiverAddress = findViewById(R.id.tv_receiver_address);
        tvOrderNote = findViewById(R.id.tv_order_note);
        llTimeline = findViewById(R.id.ll_status_timeline);
        btnUpdateStatus = findViewById(R.id.btn_update_status);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvDiscount = findViewById(R.id.tv_discount);
        tvFinalTotal = findViewById(R.id.tv_final_total);
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_order_items);
        rv.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new AdminOrderProductAdapter();
        rv.setAdapter(productAdapter);
    }

    private void onOrderLoaded(Order order) {
        if (order == null) return;
        currentOrder = order;

        String statusLabel = viewModel.getStatusLabel(order.getOrderStatus());
        tvCurrentStatus.setText(statusLabel);

        if (order.getShippingAddress() != null) {
            tvReceiverName.setText(order.getShippingAddress().getName() + " · " + order.getShippingAddress().getPhone());
            tvReceiverAddress.setText(order.getShippingAddress().getDetailAddress());
        }

        if (order.getNote() != null && !order.getNote().trim().isEmpty()) {
            tvOrderNote.setVisibility(View.VISIBLE);
            tvOrderNote.setText("Ghi chú: " + order.getNote());
        } else {
            tvOrderNote.setVisibility(View.GONE);
        }

        productAdapter.setItems(order.getItems());

        tvSubtotal.setText(PriceFormatter.formatVND(order.getTotalPrice()));
        tvShippingFee.setText(PriceFormatter.formatVND(order.getShippingFee()));
        tvDiscount.setText("-" + PriceFormatter.formatVND(order.getDiscountAmount()));
        tvFinalTotal.setText(PriceFormatter.formatVND(order.getFinalTotal()));

        buildTimeline(order);
        updateActionButtons(order);
    }

    /**
     * Vẽ timeline các bước đã qua — Model Order chỉ lưu createdAt (không lưu lịch sử đổi
     * trạng thái theo từng mốc thời gian, đã bỏ field trackingHistory từ đầu dự án), nên
     * timeline chỉ hiển thị TÊN các bước đã hoàn thành, không hiển thị thời gian riêng
     * từng bước — chỉ bước đầu tiên hiện đúng thời gian tạo đơn.
     */
    private void buildTimeline(Order order) {
        llTimeline.removeAllViews();
        String[] mainFlow = {
                Constants.ORDER_PENDING, Constants.ORDER_CONFIRMED,
                Constants.ORDER_PACKING, Constants.ORDER_SHIPPING, Constants.ORDER_DELIVERED
        };

        boolean isCancelledOrReturned = Constants.ORDER_CANCELLED.equals(order.getOrderStatus())
                || Constants.ORDER_RETURNED.equals(order.getOrderStatus());

        if (isCancelledOrReturned) {
            addTimelineStep(viewModel.getStatusLabel(order.getOrderStatus()),
                    PriceFormatter.formatDateTime(order.getCreatedAt()), true);
            return;
        }

        int currentIndex = indexOf(mainFlow, order.getOrderStatus());
        if (currentIndex == -1) return;
        
        for (int i = 0; i <= currentIndex && i < mainFlow.length; i++) {
            String time = i == 0 ? PriceFormatter.formatDateTime(order.getCreatedAt()) : "";
            addTimelineStep(viewModel.getStatusLabel(mainFlow[i]), time, i == mainFlow.length - 1);
        }
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return -1;
    }

    private void addTimelineStep(String title, String time, boolean isLast) {
        View stepView = LayoutInflater.from(this).inflate(R.layout.admin_item_timeline_step, llTimeline, false);
        TextView tvTitle = stepView.findViewById(R.id.tv_step_title);
        TextView tvTime = stepView.findViewById(R.id.tv_step_time);
        View line = stepView.findViewById(R.id.v_step_line);

        tvTitle.setText(title);
        tvTime.setText(time);
        tvTime.setVisibility(time.isEmpty() ? View.GONE : View.VISIBLE);
        if (isLast) line.setVisibility(View.INVISIBLE);

        llTimeline.addView(stepView);
    }

    private void updateActionButtons(Order order) {
        String nextStatus = viewModel.getNextStatus(order.getOrderStatus());
        boolean isFinalState = Constants.ORDER_CANCELLED.equals(order.getOrderStatus())
                || Constants.ORDER_RETURNED.equals(order.getOrderStatus())
                || Constants.ORDER_DELIVERED.equals(order.getOrderStatus());

        if (nextStatus != null) {
            btnUpdateStatus.setVisibility(View.VISIBLE);
            btnUpdateStatus.setText(viewModel.getStatusLabel(order.getOrderStatus())
                    + " → " + viewModel.getStatusLabel(nextStatus));
        } else {
            btnUpdateStatus.setVisibility(View.GONE);
        }

        btnCancelOrder.setVisibility(isFinalState || Constants.ORDER_SHIPPING.equals(order.getOrderStatus()) ? View.GONE : View.VISIBLE);
    }

    private void handleUpdateStatus() {
        if (currentOrder == null) return;
        String nextStatus = viewModel.getNextStatus(currentOrder.getOrderStatus());
        if (nextStatus == null) return;
        viewModel.updateStatus(orderId, currentOrder.getUserId(), nextStatus);
    }

    private void confirmCancel() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn chắc chắn muốn hủy đơn hàng này?")
                .setPositiveButton("Hủy đơn", (d, w) -> {
                    if (currentOrder != null) viewModel.cancelOrder(orderId, currentOrder.getUserId(), currentOrder.getOrderStatus());
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showError(String message) {
        if (message != null) android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
