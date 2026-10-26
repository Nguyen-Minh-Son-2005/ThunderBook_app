package com.example.bookapp.View.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.user.OrderItemAdapter;
import com.example.bookapp.Model.Order;
import com.example.bookapp.R;
import com.example.bookapp.ViewModel.OrderDetailViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";

    private TextView tvOrderStatus, tvAddressNamePhone, tvAddressDetail,
            tvTotalPrice, tvShippingFee, tvDiscount, tvFinalTotal, tvNote;
    private TextView tvStep1Label, tvStep2Label, tvStep3Label, tvStep4Label;
    private android.widget.ImageView step1Dot, step2Dot, step3Dot, step4Dot;
    private View stepLine1, stepLine2, stepLine3;
    private RecyclerView rvOrderItems;
    private Button btnCancelOrder, btnReviewOrder;

    private String orderId;

    private OrderDetailViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        bindViews();

        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);

        viewModel.getOrder().observe(this, order -> {
            if (order != null) bindOrderData(order);
        });

        viewModel.getCancelSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                android.widget.Toast.makeText(this, "Đã hủy đơn hàng", android.widget.Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        if (orderId != null) viewModel.loadOrder(orderId);

        btnCancelOrder.setOnClickListener(v -> confirmCancelOrder());
        btnReviewOrder.setOnClickListener(v -> {
            Order currentOrder = viewModel.getOrder().getValue();
            if (currentOrder != null && !currentOrder.getItems().isEmpty()) {
                String[] bookTitles = new String[currentOrder.getItems().size()];
                for (int i = 0; i < currentOrder.getItems().size(); i++) {
                    bookTitles[i] = currentOrder.getItems().get(i).getTitle();
                }
                new AlertDialog.Builder(this)
                        .setTitle("Chọn sách để đánh giá")
                        .setItems(bookTitles, (dialog, which) -> {
                            Intent intent = new Intent(this, WriteReviewActivity.class);
                            intent.putExtra(WriteReviewActivity.EXTRA_ORDER_ID, orderId);
                            intent.putExtra(WriteReviewActivity.EXTRA_BOOK_ID, currentOrder.getItems().get(which).getBookId());
                            startActivity(intent);
                        })
                        .show();
            }
        });
    }

    private void bindViews() {
        tvOrderStatus = findViewById(R.id.tv_order_status);
        tvStep1Label = findViewById(R.id.tv_step1_label);
        tvStep2Label = findViewById(R.id.tv_step2_label);
        tvStep3Label = findViewById(R.id.tv_step3_label);
        tvStep4Label = findViewById(R.id.tv_step4_label);
        tvAddressNamePhone = findViewById(R.id.tv_address_name_phone);
        tvAddressDetail = findViewById(R.id.tv_address_detail);
        rvOrderItems = findViewById(R.id.rv_order_items);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvDiscount = findViewById(R.id.tv_discount);
        tvFinalTotal = findViewById(R.id.tv_final_total);
        tvNote = findViewById(R.id.tv_note);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        btnReviewOrder = findViewById(R.id.btn_review_order);
        
        step1Dot = findViewById(R.id.step1_dot);
        step2Dot = findViewById(R.id.step2_dot);
        step3Dot = findViewById(R.id.step3_dot);
        step4Dot = findViewById(R.id.step4_dot);
        stepLine1 = findViewById(R.id.step_line1);
        stepLine2 = findViewById(R.id.step_line2);
        stepLine3 = findViewById(R.id.step_line3);

        rvOrderItems.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
    }

    private void bindOrderData(Order order) {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvOrderStatus.setText(mapStatusToLabel(order.getOrderStatus()));

        if (order.getShippingAddress() != null) {
            tvAddressNamePhone.setText(order.getShippingAddress().getName()
                    + "  |  " + order.getShippingAddress().getPhone());
            tvAddressDetail.setText(order.getShippingAddress().getFullAddress());
        }

        rvOrderItems.setAdapter(new OrderItemAdapter(order.getItems()));

        tvTotalPrice.setText(currencyFormat.format(order.getTotalPrice()) + "đ");
        tvShippingFee.setText(currencyFormat.format(order.getShippingFee()) + "đ");
        tvDiscount.setText("-" + currencyFormat.format(order.getDiscountAmount()) + "đ");
        tvFinalTotal.setText(currencyFormat.format(order.getFinalTotal()) + "đ");
        tvNote.setText("Ghi chú: " + (order.getNote() == null || order.getNote().isEmpty()
                ? "Không có" : order.getNote()));

        // Chỉ cho hủy khi đơn còn ở trạng thái đầu, chỉ cho đánh giá khi đã giao xong
        boolean canCancel = "pending".equals(order.getOrderStatus());
        boolean canReview = "delivered".equals(order.getOrderStatus());

        btnCancelOrder.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        btnReviewOrder.setVisibility(canReview ? View.VISIBLE : View.GONE);
        
        updateOrderProgress(order.getOrderStatus());
    }

    private void updateOrderProgress(String status) {
        if (status == null) return;
        
        int s1 = 0, s2 = 0, s3 = 0, s4 = 0;
        int l1 = 0, l2 = 0, l3 = 0;
        int t1 = 0, t2 = 0, t3 = 0, t4 = 0;

        if ("pending".equals(status)) {
            s1 = 1; t1 = 1;
        } else if ("confirmed".equals(status) || "packing".equals(status)) {
            s1 = 2; s2 = 1; l1 = 1; t1 = 2; t2 = 1;
        } else if ("shipping".equals(status)) {
            s1 = 2; s2 = 2; s3 = 0; l1 = 1; l2 = 0;
            t1 = 2; t2 = 2; t3 = 1;
        } else if ("delivered".equals(status)) {
            s1 = 2; s2 = 2; s3 = 2; s4 = 2; l1 = 1; l2 = 1; l3 = 1;
            t1 = 2; t2 = 2; t3 = 2; t4 = 1;
        } else if ("cancelled".equals(status)) {
            // Đơn bị hủy — tất cả dot/line đều inactive (xám)
            s1 = 0; s2 = 0; s3 = 0; s4 = 0;
            l1 = 0; l2 = 0; l3 = 0;
            t1 = 0; t2 = 0; t3 = 0; t4 = 0;
        } else {
            s1 = 1; t1 = 1;
        }

        setStepState(step1Dot, s1);
        setStepState(step2Dot, s2);
        setStepState(step3Dot, s3);
        setStepState(step4Dot, s4);
        
        setStepLabelState(tvStep1Label, t1);
        setStepLabelState(tvStep2Label, t2);
        setStepLabelState(tvStep3Label, t3);
        setStepLabelState(tvStep4Label, t4);

        stepLine1.setBackgroundResource(l1 == 1 ? R.color.status_success : R.color.divider);
        stepLine2.setBackgroundResource(l2 == 1 ? R.color.status_success : R.color.divider);
        stepLine3.setBackgroundResource(l3 == 1 ? R.color.status_success : R.color.divider);
    }

    private void setStepState(android.widget.ImageView dot, int state) {
        if (state == 0) {
            dot.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(this, R.color.divider));
            dot.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.text_hint), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (state == 1) {
            dot.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(this, R.color.primary));
            dot.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (state == 2) {
            dot.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(this, R.color.status_success));
            dot.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void setStepLabelState(TextView tv, int state) {
        if (tv == null) return;
        if (state == 0) {
            tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_hint));
            tv.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else if (state == 1) {
            tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.primary));
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (state == 2) {
            tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_primary));
            tv.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private String mapStatusToLabel(String status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case "pending": return "Chờ xác nhận";
            case "confirmed": return "Đã xác nhận";
            case "packing": return "Đang đóng gói";
            case "shipping": return "Đang giao";
            case "delivered": return "Đã giao";
            case "cancelled": return "Đã hủy";
            case "returned": return "Trả hàng";
            default: return status;
        }
    }

    private void confirmCancelOrder() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có xác nhận hủy đơn hàng này?")
                .setPositiveButton("Đồng ý", (dialog, which) -> cancelOrder())
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void cancelOrder() {
        if (orderId == null) return;
        viewModel.cancelOrder(orderId);
    }
}
