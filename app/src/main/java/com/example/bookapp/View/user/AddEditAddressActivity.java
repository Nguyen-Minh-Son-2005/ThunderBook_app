package com.example.bookapp.View.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookapp.Model.Address;
import com.example.bookapp.R;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.ViewModel.AddEditAddressViewModel;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddEditAddressActivity extends AppCompatActivity {

    public static final String EXTRA_ADDRESS_ID = "extra_address_id";

    private TextInputEditText etName, etPhone, etDetailAddress;
    private AutoCompleteTextView actvProvince, actvDistrict, actvWard;
    private Button btnSave;

    private String editingAddressId = null; // null = đang thêm mới
    private AddEditAddressViewModel viewModel;

    // API URL
    private static final String API_URL = "https://provinces.open-api.vn/api/";
    private final OkHttpClient client = new OkHttpClient();

    // Data lists
    private final List<LocationItem> provinceList = new ArrayList<>();
    private final List<LocationItem> districtList = new ArrayList<>();
    private final List<LocationItem> wardList = new ArrayList<>();

    // Selected items
    private LocationItem selectedProvince;
    private LocationItem selectedDistrict;
    private LocationItem selectedWard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_address);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etDetailAddress = findViewById(R.id.et_detail_address);
        actvProvince = findViewById(R.id.actv_province);
        actvDistrict = findViewById(R.id.actv_district);
        actvWard = findViewById(R.id.actv_ward);
        btnSave = findViewById(R.id.btn_save_address);

        editingAddressId = getIntent().getStringExtra(EXTRA_ADDRESS_ID);
        if (editingAddressId != null) {
            toolbar.setTitle("Sửa địa chỉ");
        }

        viewModel = new ViewModelProvider(this).get(AddEditAddressViewModel.class);

        // Observe địa chỉ hiện có (chỉ dùng khi ở chế độ sửa)
        viewModel.getAddress().observe(this, address -> {
            if (address == null) return;
            etName.setText(address.getName());
            etPhone.setText(address.getPhone());
            actvProvince.setText(address.getProvince(), false);
            actvDistrict.setText(address.getDistrict(), false);
            actvWard.setText(address.getWard(), false);
            etDetailAddress.setText(address.getDetailAddress());
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Đã lưu địa chỉ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Không thể lưu địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });

        // Load địa chỉ hiện có nếu đang ở chế độ sửa
        if (editingAddressId != null) {
            String uid = FirebaseUtils.getCurrentUserId();
            if (uid != null) viewModel.loadAddress(uid, editingAddressId);
        }

        btnSave.setOnClickListener(v -> saveAddress());

        // Setup Dropdowns
        setupDropdownListeners();

        // Tải Tỉnh/Thành đầu tiên
        loadProvinces();
    }

    private void setupDropdownListeners() {
        actvProvince.setOnItemClickListener((parent, view, position, id) -> {
            selectedProvince = provinceList.get(position);
            selectedDistrict = null;
            selectedWard = null;
            actvDistrict.setText("", false);
            actvWard.setText("", false);
            districtList.clear();
            wardList.clear();
            loadDistricts(selectedProvince.code);
        });

        actvDistrict.setOnItemClickListener((parent, view, position, id) -> {
            selectedDistrict = districtList.get(position);
            selectedWard = null;
            actvWard.setText("", false);
            wardList.clear();
            loadWards(selectedDistrict.code);
        });

        actvWard.setOnItemClickListener((parent, view, position, id) -> {
            selectedWard = wardList.get(position);
        });
    }

    private void loadProvinces() {
        Request request = new Request.Builder().url(API_URL + "p/").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(AddEditAddressActivity.this, "Lỗi tải Tỉnh/Thành", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONArray jsonArray = new JSONArray(json);
                        provinceList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            provinceList.add(new LocationItem(obj.getString("code"), obj.getString("name")));
                        }
                        runOnUiThread(() -> {
                            ArrayAdapter<LocationItem> adapter = new ArrayAdapter<>(AddEditAddressActivity.this,
                                    R.layout.item_dropdown, provinceList);
                            actvProvince.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void loadDistricts(String provinceCode) {
        Request request = new Request.Builder().url(API_URL + "p/" + provinceCode + "?depth=2").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) { }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject provinceObj = new JSONObject(json);
                        JSONArray districtsArray = provinceObj.getJSONArray("districts");
                        districtList.clear();
                        for (int i = 0; i < districtsArray.length(); i++) {
                            JSONObject obj = districtsArray.getJSONObject(i);
                            districtList.add(new LocationItem(obj.getString("code"), obj.getString("name")));
                        }
                        runOnUiThread(() -> {
                            ArrayAdapter<LocationItem> adapter = new ArrayAdapter<>(AddEditAddressActivity.this,
                                    R.layout.item_dropdown, districtList);
                            actvDistrict.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void loadWards(String districtCode) {
        Request request = new Request.Builder().url(API_URL + "d/" + districtCode + "?depth=2").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) { }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject districtObj = new JSONObject(json);
                        JSONArray wardsArray = districtObj.getJSONArray("wards");
                        wardList.clear();
                        for (int i = 0; i < wardsArray.length(); i++) {
                            JSONObject obj = wardsArray.getJSONObject(i);
                            wardList.add(new LocationItem(obj.getString("code"), obj.getString("name")));
                        }
                        runOnUiThread(() -> {
                            ArrayAdapter<LocationItem> adapter = new ArrayAdapter<>(AddEditAddressActivity.this,
                                    R.layout.item_dropdown, wardList);
                            actvWard.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void saveAddress() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String province = actvProvince.getText().toString().trim();
        String district = actvDistrict.getText().toString().trim();
        String ward = actvWard.getText().toString().trim();
        String detail = etDetailAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(province)
                || TextUtils.isEmpty(district) || TextUtils.isEmpty(ward) || TextUtils.isEmpty(detail)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("^(0|\\+84)[3|5|7|8|9]\\d{8}$")) {
            Toast.makeText(this, "Số điện thoại không hợp lệ (VD: 0912345678 hoặc +84912345678)", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseUtils.getCurrentUserId();
        if (uid == null) return;

        String addressId = editingAddressId != null ? editingAddressId : UUID.randomUUID().toString();
        Address address = new Address(addressId, name, phone, province, district, ward, detail);

        viewModel.saveAddress(uid, address);
    }

    // Lớp trợ giúp hiển thị text trên Dropdown
    private static class LocationItem {
        String code;
        String name;

        LocationItem(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
}
