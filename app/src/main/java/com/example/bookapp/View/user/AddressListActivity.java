package com.example.bookapp.View.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.user.AddressAdapter;
import com.example.bookapp.Model.Address;
import com.example.bookapp.R;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.ViewModel.AddressListViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Dùng chung cho 2 mục đích:
 * 1) Quản lý sổ địa chỉ (mở từ ProfileFragment) -> chỉ có Sửa/Xóa/Thêm
 * 2) Chọn địa chỉ giao hàng (mở từ CheckoutActivity với EXTRA_SELECT_MODE = true)
 *    -> hiện thêm RadioButton + nút "Dùng địa chỉ này", trả kết quả qua setResult()
 */
public class AddressListActivity extends AppCompatActivity {

    public static final String EXTRA_SELECT_MODE = "extra_select_mode";
    public static final String EXTRA_SELECTED_ADDRESS = "extra_selected_address";

    private RecyclerView rvAddresses;
    private LinearLayout llEmpty;
    private Button btnUseAddress;
    private ImageButton fabAddAddress;

    private AddressAdapter adapter;
    private final List<Address> addressList = new ArrayList<>();
    private boolean selectMode;

    private AddressListViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        selectMode = getIntent().getBooleanExtra(EXTRA_SELECT_MODE, false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (selectMode) {
            toolbar.setTitle("Chọn địa chỉ giao hàng");
        }

        rvAddresses = findViewById(R.id.rv_addresses);
        llEmpty = findViewById(R.id.ll_empty);
        btnUseAddress = findViewById(R.id.btn_use_address);
        fabAddAddress = findViewById(R.id.fab_add_address);

        btnUseAddress.setVisibility(selectMode ? View.VISIBLE : View.GONE);

        rvAddresses.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        adapter = new AddressAdapter(addressList, selectMode, new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onSelect(Address address) {
                // Chỉ cập nhật lựa chọn hiện tại trong adapter, xác nhận khi bấm btn_use_address
            }

            @Override
            public void onEdit(Address address) {
                Intent intent = new Intent(AddressListActivity.this, AddEditAddressActivity.class);
                intent.putExtra(AddEditAddressActivity.EXTRA_ADDRESS_ID, address.getAddressId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Address address) {
                confirmDelete(address);
            }
        });
        rvAddresses.setAdapter(adapter);

        fabAddAddress.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditAddressActivity.class)));

        btnUseAddress.setOnClickListener(v -> confirmSelection());

        viewModel = new ViewModelProvider(this).get(AddressListViewModel.class);

        viewModel.getAddresses().observe(this, addresses -> {
            addressList.clear();
            if (addresses != null) addressList.addAll(addresses);
            adapter.notifyDataSetChanged();

            boolean isEmpty = addressList.isEmpty();
            llEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvAddresses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid != null) viewModel.loadAddresses(uid);
    }

    private void confirmSelection() {
        String selectedId = adapter.getSelectedAddressId();
        if (selectedId == null) {
            Toast.makeText(this, "Vui lòng chọn 1 địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Address address : addressList) {
            if (address.getAddressId().equals(selectedId)) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_ADDRESS, address.getAddressId());
                setResult(RESULT_OK, resultIntent);
                finish();
                return;
            }
        }
    }

    private void confirmDelete(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ")
                .setMessage("Bạn có chắc muốn xóa địa chỉ này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    String uid = FirebaseUtils.getCurrentUserId();
                    if (uid != null) viewModel.deleteAddress(uid, address.getAddressId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
