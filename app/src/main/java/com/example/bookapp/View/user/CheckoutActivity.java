package com.example.bookapp.View.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.user.OrderItemAdapter;
import com.example.bookapp.Model.Address;
import com.example.bookapp.Model.OrderItem;
import com.example.bookapp.Model.Voucher;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.ViewModel.CheckoutViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Mở từ CartFragment với danh sách bookId đã chọn (EXTRA_SELECTED_BOOK_IDS).
 * Logic recalculateTotal() giữ trong Activity (thuần UI math, không gọi Firestore).
 */
public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_BOOK_IDS = "extra_selected_book_ids";

    private static final double DEFAULT_SHIPPING_FEE = 30000;

    private TextView tvAddressNamePhone, tvAddressDetail, tvSubtotal, tvShippingFee,
            tvDiscount, tvFinalTotal, tvVoucherStatus;
    private LinearLayout llSelectAddress, llSelectVoucher, llPlaceOrderBar;
    private RecyclerView rvCheckoutItems;
    private RadioGroup rgPaymentMethod;
    private EditText etNote;
    private Button btnPlaceOrder;

    private final List<String> selectedBookIds = new ArrayList<>();
    private Address currentSelectedAddress = null;
    private Voucher currentSelectedVoucher = null;
    private double shippingFee = DEFAULT_SHIPPING_FEE;
    private List<OrderItem> currentOrderItems = new ArrayList<>();

    private CheckoutViewModel viewModel;

    private final ActivityResultLauncher<Intent> selectAddressLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String addressId = result.getData()
                            .getStringExtra(AddressListActivity.EXTRA_SELECTED_ADDRESS);
                    String uid = FirebaseUtils.getCurrentUserId();
                    if (addressId != null && uid != null) {
                        viewModel.loadAddress(uid, addressId);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> selectVoucherLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Voucher voucher = result.getData()
                            .getParcelableExtra(SelectVoucherActivity.EXTRA_SELECTED_VOUCHER);
                    if (voucher != null) {
                        // Set trực tiếp vào LiveData — KHÔNG cần query Firestore lại
                        viewModel.setSelectedVoucher(voucher);
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        List<String> ids = getIntent().getStringArrayListExtra(EXTRA_SELECTED_BOOK_IDS);
        if (ids != null) selectedBookIds.addAll(ids);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        bindViews();
        setupClicks();

        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        viewModel.getOrderItems().observe(this, orderItems -> {
            if (orderItems != null) {
                currentOrderItems = orderItems;
                rvCheckoutItems.setAdapter(new OrderItemAdapter(orderItems));
                recalculateTotal();
            }
        });

        viewModel.getSelectedAddress().observe(this, address -> {
            currentSelectedAddress = address;
            if (address != null) {
                tvAddressNamePhone.setText(address.getName() + "  |  " + address.getPhone());
                tvAddressDetail.setText(address.getFullAddress());
            } else {
                tvAddressNamePhone.setText("Chọn địa chỉ giao hàng");
                tvAddressDetail.setText("Nhấn vào đây để thêm hoặc chọn địa chỉ nhận hàng");
            }
        });

        viewModel.getSelectedVoucher().observe(this, voucher -> {
            currentSelectedVoucher = voucher;
            if (voucher != null) {
                String desc = "";
                if (Constants.VOUCHER_PERCENT.equals(voucher.getKind())) {
                    desc = " (Giảm " + (int) voucher.getValue() + "%)";
                } else if (Constants.VOUCHER_FIXED.equals(voucher.getKind())) {
                    desc = " (Giảm " + (int) voucher.getValue() + "đ)";
                } else if (Constants.VOUCHER_FREESHIP.equals(voucher.getKind())) {
                    desc = " (Miễn phí vận chuyển)";
                }
                tvVoucherStatus.setText("Đã áp dụng mã " + voucher.getCode() + desc);
                if (Constants.VOUCHER_FREESHIP.equals(voucher.getKind())) {
                    shippingFee = 0;
                } else {
                    shippingFee = DEFAULT_SHIPPING_FEE;
                }
                recalculateTotal();
            } else {
                shippingFee = DEFAULT_SHIPPING_FEE;
                tvVoucherStatus.setText("Chọn hoặc nhập mã giảm giá");
                recalculateTotal();
            }
        });

        viewModel.getPlaceOrderResult().observe(this, orderId -> {
            if (orderId != null) {
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, OrderDetailActivity.class);
                intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, orderId);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(this, "Đặt hàng thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });

        String uid = FirebaseUtils.getCurrentUserId();
        if (uid != null) {
            viewModel.loadCheckoutItems(uid, selectedBookIds);
            viewModel.loadDefaultAddress(uid);
        }
    }

    private void bindViews() {
        tvAddressNamePhone = findViewById(R.id.tv_address_name_phone);
        tvAddressDetail = findViewById(R.id.tv_address_detail);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvDiscount = findViewById(R.id.tv_discount);
        tvFinalTotal = findViewById(R.id.tv_final_total);
        tvVoucherStatus = findViewById(R.id.tv_voucher_status);
        llSelectAddress = findViewById(R.id.ll_select_address);
        llSelectVoucher = findViewById(R.id.ll_select_voucher);
        llPlaceOrderBar = findViewById(R.id.ll_place_order_bar);
        rvCheckoutItems = findViewById(R.id.rv_checkout_items);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        etNote = findViewById(R.id.et_note);
        btnPlaceOrder = findViewById(R.id.btn_place_order);

        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClicks() {
        llSelectAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressListActivity.class);
            intent.putExtra(AddressListActivity.EXTRA_SELECT_MODE, true);
            selectAddressLauncher.launch(intent);
        });

        llSelectVoucher.setOnClickListener(v ->
                selectVoucherLauncher.launch(new Intent(this, SelectVoucherActivity.class)));

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        if (currentSelectedAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentOrderItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseUtils.getCurrentUserId();
        if (uid == null) return;

        btnPlaceOrder.setEnabled(false);

        double subtotal = getSubtotal();
        double discount = currentSelectedVoucher != null
                ? currentSelectedVoucher.calculateDiscount(subtotal) : 0;

        viewModel.placeOrder(
                uid, currentOrderItems, currentSelectedAddress, currentSelectedVoucher,
                subtotal, shippingFee, discount,
                etNote.getText().toString().trim(),
                selectedBookIds
        );
    }

    private double getSubtotal() {
        double subtotal = 0;
        for (OrderItem item : currentOrderItems) {
            subtotal += item.getLineTotal();
        }
        return subtotal;
    }

    private void recalculateTotal() {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        double subtotal = getSubtotal();
        double discount = currentSelectedVoucher != null
                ? currentSelectedVoucher.calculateDiscount(subtotal) : 0;
        double finalTotal = subtotal + shippingFee - discount;

        tvSubtotal.setText(currencyFormat.format(subtotal) + "đ");
        tvShippingFee.setText(currencyFormat.format(shippingFee) + "đ");
        tvDiscount.setText("-" + currencyFormat.format(discount) + "đ");
        tvFinalTotal.setText(currencyFormat.format(Math.max(finalTotal, 0)) + "đ");
    }
}
