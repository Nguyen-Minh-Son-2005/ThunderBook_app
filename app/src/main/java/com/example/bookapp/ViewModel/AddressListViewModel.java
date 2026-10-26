package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Address;
import com.example.bookapp.Repository.AddressRepository;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.List;

/**
 * ViewModel cho AddressListActivity.
 * Tách load/xóa địa chỉ khỏi Activity.
 */
public class AddressListViewModel extends ViewModel {

    private final AddressRepository addressRepository = new AddressRepository();

    private final MutableLiveData<List<Address>> _addresses = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<List<Address>> getAddresses() { return _addresses; }
    public LiveData<Boolean> getDeleteSuccess() { return _deleteSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    public void loadAddresses(String uid) {
        addressRepository.getAddresses(uid).observeForever(
                addresses -> _addresses.setValue(addresses));
    }

    public void deleteAddress(String uid, String addressId) {
        addressRepository.deleteAddress(uid, addressId, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _deleteSuccess.setValue(true);
                // Reload danh sách sau khi xóa
                loadAddresses(uid);
            }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.setValue(e.getMessage());
            }
        });
    }
}
