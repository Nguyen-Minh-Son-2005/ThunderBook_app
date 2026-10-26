package com.example.bookapp.View.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookapp.Model.Voucher;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.PriceFormatter;
import com.example.bookapp.ViewModel.AdminAddEditVoucherViewModel;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Date;

public class AddEditVoucherActivity extends AdminBaseActivity {

    private AdminAddEditVoucherViewModel viewModel;

    private TextInputEditText etCode, etValue, etStartDate, etEndDate;
    private ChipGroup cgType;
    private SwitchCompat swActive;

    private String editingVoucherId = null;
    private Date selectedStartDate, selectedEndDate;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_add_edit_voucher);

        viewModel = new ViewModelProvider(this).get(AdminAddEditVoucherViewModel.class);

        bindViews();
        setupToolbar(findViewById(R.id.tb_toolbar), "Thêm voucher");
        setupDatePickers();

        editingVoucherId = getIntent().getStringExtra(Constants.EXTRA_VOUCHER_ID);
        boolean isEditMode = getIntent().getBooleanExtra(Constants.EXTRA_MODE_EDIT, false);
        if (isEditMode && editingVoucherId != null) {
            setupToolbar(findViewById(R.id.tb_toolbar), "Sửa voucher");
            setupDeleteAction();
            viewModel.loadVoucherForEdit(editingVoucherId);
        }

        observeViewModel();

        findViewById(R.id.btn_save_voucher).setOnClickListener(v -> saveVoucher());
    }

    private void bindViews() {
        etCode = findViewById(R.id.et_voucher_code);
        etValue = findViewById(R.id.et_voucher_value);
        etStartDate = findViewById(R.id.et_voucher_start_date);
        etEndDate = findViewById(R.id.et_voucher_end_date);
        cgType = findViewById(R.id.cg_voucher_type);
        swActive = findViewById(R.id.sw_voucher_active_form);

        cgType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_type_freeship) {
                etValue.setText("30000");
                etValue.setEnabled(false);
            } else {
                etValue.setEnabled(true);
            }
        });
    }

    /** Nút xóa dùng ở thanh công cụ bên dưới. */
    private void setupDeleteAction() {
        View btnDelete = findViewById(R.id.btn_delete_voucher);
        btnDelete.setVisibility(View.VISIBLE);
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void confirmDelete() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa voucher")
                .setMessage("Bạn chắc chắn muốn xóa voucher này? Không thể hoàn tác.")
                .setPositiveButton("Xóa", (d, w) -> viewModel.deleteVoucher(editingVoucherId))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar cal = Calendar.getInstance();
        Date existing = isStartDate ? selectedStartDate : selectedEndDate;
        if (existing != null) cal.setTime(existing);

        new DatePickerDialog(this, R.style.CustomDatePickerTheme, (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth, 0, 0, 0);
            Date pickedDate = picked.getTime();
            if (isStartDate) {
                selectedStartDate = pickedDate;
                etStartDate.setText(PriceFormatter.formatDate(pickedDate));
            } else {
                selectedEndDate = pickedDate;
                etEndDate.setText(PriceFormatter.formatDate(pickedDate));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void observeViewModel() {
        viewModel.getLoadedVoucher().observe(this, this::populateForm);

        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Đã lưu voucher", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getDeleteSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Đã xóa voucher", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void populateForm(Voucher voucher) {
        if (voucher == null) return;
        etCode.setText(voucher.getCode());
        etValue.setText(String.valueOf((long) voucher.getValue()));
        swActive.setChecked(voucher.isActive());

        selectedStartDate = voucher.getStartDate() != null ? voucher.getStartDate().toDate() : null;
        selectedEndDate = voucher.getEndDate() != null ? voucher.getEndDate().toDate() : null;
        etStartDate.setText(PriceFormatter.formatDate(voucher.getStartDate()));
        etEndDate.setText(PriceFormatter.formatDate(voucher.getEndDate()));

        int checkedId = R.id.chip_type_percent;
        if (Constants.VOUCHER_FIXED.equals(voucher.getKind())) checkedId = R.id.chip_type_fixed;
        else if (Constants.VOUCHER_FREESHIP.equals(voucher.getKind())) checkedId = R.id.chip_type_freeship;
        cgType.check(checkedId);
    }

    private void saveVoucher() {
        String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
        String valueText = etValue.getText() != null ? etValue.getText().toString().trim() : "0";
        double value;
        try {
            value = Double.parseDouble(valueText);
        } catch (NumberFormatException e) {
            value = 0;
        }

        String type;
        int checkedId = cgType.getCheckedChipId();
        if (checkedId == R.id.chip_type_fixed) {
            type = Constants.VOUCHER_FIXED;
        } else if (checkedId == R.id.chip_type_freeship) {
            type = Constants.VOUCHER_FREESHIP;
            value = 30000; // Miễn phí ship luôn giảm 30.000 đ
        } else {
            type = Constants.VOUCHER_PERCENT;
        }

        viewModel.saveVoucher(editingVoucherId, code, type, value,
                selectedStartDate, selectedEndDate, swActive.isChecked());
    }
}
