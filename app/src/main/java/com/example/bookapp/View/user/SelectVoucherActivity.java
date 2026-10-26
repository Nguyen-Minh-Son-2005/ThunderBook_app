package com.example.bookapp.View.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.user.VoucherAdapter;
import com.example.bookapp.Model.Voucher;
import com.example.bookapp.R;
import com.example.bookapp.ViewModel.SelectVoucherViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Mở từ CheckoutActivity.
 * Khi user chọn hoặc nhập mã hợp lệ → trả Voucher Parcelable về CheckoutActivity ngay lập tức.
 */
public class SelectVoucherActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_VOUCHER = "extra_selected_voucher";

    private EditText etVoucherCode;
    private TextView tvApplyCode;
    private RecyclerView rvVouchers;
    private Button btnConfirm;

    private final List<Voucher> voucherList = new ArrayList<>();
    private Voucher selectedVoucher = null;

    private SelectVoucherViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_voucher);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etVoucherCode = findViewById(R.id.et_voucher_code);
        tvApplyCode   = findViewById(R.id.tv_apply_code);
        rvVouchers    = findViewById(R.id.rv_vouchers);
        btnConfirm    = findViewById(R.id.btn_confirm_voucher);

        rvVouchers.setLayoutManager(new LinearLayoutManager(this));

        // Khi chọn item từ danh sách → lưu đối tượng đầy đủ và fill code lên ô
        VoucherAdapter adapter = new VoucherAdapter(voucherList, voucher -> {
            selectedVoucher = voucher;
            etVoucherCode.setText(voucher.getCode());
        });
        rvVouchers.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(SelectVoucherViewModel.class);

        viewModel.getVouchers().observe(this, vouchers -> {
            voucherList.clear();
            if (vouchers != null) {
                voucherList.addAll(vouchers);
            }
            adapter.notifyDataSetChanged();
        });

        viewModel.loadAvailableVouchers();

        tvApplyCode.setOnClickListener(v -> applyManualCode());
        btnConfirm.setOnClickListener(v -> confirmAndReturn());
    }

    /**
     * Tìm mã trong danh sách đã tải → không query Firestore lần 2.
     */
    private void applyManualCode() {
        String code = etVoucherCode.getText().toString().trim().toUpperCase();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
            return;
        }
        Voucher found = findInList(code);
        if (found != null) {
            selectedVoucher = found;
            Toast.makeText(this, "Đã chọn mã: " + code, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Mã không tồn tại hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Trả Voucher object đầy đủ về CheckoutActivity.
     */
    private void confirmAndReturn() {
        // Nếu chưa chọn, thử tìm theo code đang gõ trong ô
        if (selectedVoucher == null) {
            String code = etVoucherCode.getText().toString().trim().toUpperCase();
            if (!TextUtils.isEmpty(code)) {
                selectedVoucher = findInList(code);
            }
        }

        if (selectedVoucher == null) {
            Toast.makeText(this, "Vui lòng chọn hoặc nhập mã giảm giá hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent result = new Intent();
        result.putExtra(EXTRA_SELECTED_VOUCHER, selectedVoucher);
        setResult(RESULT_OK, result);
        finish();
    }

    private Voucher findInList(String code) {
        for (Voucher v : voucherList) {
            if (code.equalsIgnoreCase(v.getCode())) return v;
        }
        return null;
    }
}
