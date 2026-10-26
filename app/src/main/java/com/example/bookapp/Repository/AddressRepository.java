package com.example.bookapp.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Address;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DUY NHẤT được phép gọi Firestore cho subcollection
 * "users/{uid}/addresses". Dùng bởi AddressListActivity, AddEditAddressActivity,
 * CheckoutActivity (chọn địa chỉ giao hàng).
 */
public class AddressRepository {

    public LiveData<List<Address>> getAddresses(String uid) {
        MutableLiveData<List<Address>> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore()
                .collection(Constants.COLLECTION_USERS).document(uid)
                .collection(Constants.SUBCOLLECTION_ADDRESSES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Address> addresses = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Address address = doc.toObject(Address.class);
                        address.setAddressId(doc.getId());
                        addresses.add(address);
                    });
                    liveData.setValue(addresses);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    /** Tải 1 địa chỉ cụ thể - dùng khi mở AddEditAddressActivity ở chế độ sửa. */
    public LiveData<Address> getAddress(String uid, String addressId) {
        MutableLiveData<Address> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore()
                .collection(Constants.COLLECTION_USERS).document(uid)
                .collection(Constants.SUBCOLLECTION_ADDRESSES).document(addressId)
                .get()
                .addOnSuccessListener(doc -> liveData.setValue(doc.toObject(Address.class)))
                .addOnFailureListener(e -> liveData.setValue(null));

        return liveData;
    }

    /** Tạo mới hoặc ghi đè địa chỉ (addressId trùng nhau -> update, khác -> tạo mới). */
    public void saveAddress(String uid, Address address, FirebaseCallback<Void> callback) {
        FirebaseUtils.getFirestore()
                .collection(Constants.COLLECTION_USERS).document(uid)
                .collection(Constants.SUBCOLLECTION_ADDRESSES).document(address.getAddressId())
                .set(address)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteAddress(String uid, String addressId, FirebaseCallback<Void> callback) {
        FirebaseUtils.getFirestore()
                .collection(Constants.COLLECTION_USERS).document(uid)
                .collection(Constants.SUBCOLLECTION_ADDRESSES).document(addressId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}
