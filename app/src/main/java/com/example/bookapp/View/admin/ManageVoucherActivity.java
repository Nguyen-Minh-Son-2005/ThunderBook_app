package com.example.bookapp.View.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.admin.AdminVoucherAdapter;
import com.example.bookapp.Model.Voucher;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.ViewModel.AdminVoucherViewModel;

import java.util.List;

public class ManageVoucherActivity extends AdminBaseActivity {

    private AdminVoucherViewModel viewModel;
    private AdminVoucherAdapter adapter;
    private View emptyState;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_manage_voucher);

        viewModel = new ViewModelProvider(this).get(AdminVoucherViewModel.class);

        setupToolbar(findViewById(R.id.tb_toolbar), "Quản lý voucher");
        setupSearch();
        setupRecyclerView();
        setupFab();

        emptyState = findViewById(R.id.empty_state);

        viewModel.getDisplayedVouchers().observe(this, this::onListChanged);
        viewModel.getErrorMessage().observe(this, this::showError);
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.setHint("Tìm theo mã voucher");
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
        flSort.setOnClickListener(v -> {
            android.widget.PopupMenu menu = new android.widget.PopupMenu(this, v);
            menu.getMenu().add(0, 1, 0, "Sắp hết hạn");
            menu.getMenu().add(0, 2, 1, "Giá trị giảm cao nhất");
            menu.setOnMenuItemClickListener(item -> {
                viewModel.setSortOption(item.getItemId() == 2
                        ? AdminVoucherViewModel.SortOption.VALUE_DESC
                        : AdminVoucherViewModel.SortOption.EXPIRING_FIRST);
                return true;
            });
            menu.show();
        });
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_voucher_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminVoucherAdapter(new AdminVoucherAdapter.OnVoucherActionListener() {
            @Override
            public void onItemClick(Voucher voucher) {
                Intent intent = new Intent(ManageVoucherActivity.this, AddEditVoucherActivity.class);
                intent.putExtra(Constants.EXTRA_VOUCHER_ID, voucher.getVoucherId());
                intent.putExtra(Constants.EXTRA_MODE_EDIT, true);
                startActivity(intent);
            }

            @Override
            public void onToggleActive(Voucher voucher, boolean newActiveState) {
                viewModel.toggleActive(voucher.getVoucherId(), newActiveState);
            }
        });
        rv.setAdapter(adapter);
    }

    private void setupFab() {
        findViewById(R.id.fab_add_voucher).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditVoucherActivity.class)));
    }

    private void onListChanged(List<Voucher> vouchers) {
        adapter.setItems(vouchers);
        findViewById(R.id.tv_result_count).setVisibility(View.VISIBLE); // giữ nguyên, chỉ đảm bảo tồn tại
        ((android.widget.TextView) findViewById(R.id.tv_result_count))
                .setText(vouchers.size() + " voucher đang hoạt động");
        emptyState.setVisibility(vouchers.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
