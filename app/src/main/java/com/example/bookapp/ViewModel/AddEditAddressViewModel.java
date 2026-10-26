package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Address;
import com.example.bookapp.Repository.AddressRepository;
import com.example.bookapp.Utils.FirebaseCallback;

/**
 * ViewModel cho AddEditAddressActivity.
 * Xử lý load địa chỉ hiện có (chế độ sửa) và lưu địa chỉ mới/cũ.
 */
public class AddEditAddressViewModel extends ViewModel {

    private final AddressRepository addressRepository = new AddressRepository();

    private final MutableLiveData<Address> _address = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<Address> getAddress() { return _address; }
    public LiveData<Boolean> getSaveSuccess() { return _saveSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    /** Tải địa chỉ hiện có để điền vào form — chỉ gọi khi ở chế độ sửa (editingAddressId != null). */
    public void loadAddress(String uid, String addressId) {
        addressRepository.getAddress(uid, addressId).observeForever(
                address -> _address.setValue(address));
    }

    public void saveAddress(String uid, Address address) {
        addressRepository.saveAddress(uid, address, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _saveSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.setValue(e.getMessage());
            }
        });
    }
}
