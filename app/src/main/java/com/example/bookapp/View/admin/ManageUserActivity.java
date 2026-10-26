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

import com.example.bookapp.Adapter.admin.AdminUserAdapter;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.ViewModel.AdminUserViewModel;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ManageUserActivity extends AdminBaseActivity {

    private AdminUserViewModel viewModel;
    private AdminUserAdapter adapter;
    private View emptyState;
    private TextView tvResultCount;

    @Override
    protected void onAdminAccessGranted(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.admin_activity_manage_user);

        viewModel = new ViewModelProvider(this).get(AdminUserViewModel.class);

        setupToolbar(findViewById(R.id.tb_toolbar), "Quản lý người dùng");
        setupSearch();
        setupRoleFilter();
        setupRecyclerView();

        emptyState = findViewById(R.id.empty_state);
        tvResultCount = findViewById(R.id.tv_result_count);

        viewModel.getDisplayedUsers().observe(this, this::onListChanged);
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.setHint("Tìm theo tên / email");
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
            menu.getMenu().add(0, 1, 0, "Mặc định");
            menu.getMenu().add(0, 2, 1, "Tên A-Z");
            menu.setOnMenuItemClickListener(item -> {
                viewModel.setSortOption(item.getItemId() == 2
                        ? AdminUserViewModel.SortOption.NAME_ASC
                        : AdminUserViewModel.SortOption.DEFAULT);
                return true;
            });
            menu.show();
        });
    }

    private void setupRoleFilter() {
        ChipGroup chipGroup = findViewById(R.id.cg_user_filter);
        chipGroup.setSingleSelection(true);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_role_user) {
                viewModel.setRoleFilter(Constants.ROLE_USER);
            } else if (id == R.id.chip_role_admin) {
                viewModel.setRoleFilter(Constants.ROLE_ADMIN);
            } else {
                viewModel.setRoleFilter(null);
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_user_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(user -> {
            Intent intent = new Intent(this, AdminUserDetailActivity.class);
            intent.putExtra(Constants.EXTRA_USER_ID, user.getUserId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }

    private void onListChanged(List<com.example.bookapp.Model.User> users) {
        adapter.setItems(users);
        emptyState.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
        tvResultCount.setText(users.size() + " người dùng");
    }
}
