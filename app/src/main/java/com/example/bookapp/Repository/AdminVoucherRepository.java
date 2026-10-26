package com.example.bookapp.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Voucher;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminVoucherRepository {

    private final CollectionReference vouchersRef =
            FirebaseUtils.getFirestore().collection(Constants.COLLECTION_VOUCHERS);

    /** Parse thủ công DocumentSnapshot → Voucher */
    private Voucher fromDoc(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        Voucher v = new Voucher();
        v.setVoucherId(doc.getId());
        v.setCode(doc.getString("code"));
        v.setKind(doc.getString("kind"));
        Double value = doc.getDouble("value");
        v.setValue(value != null ? value : 0);
        v.setStartDate(doc.getTimestamp("startDate"));
        v.setEndDate(doc.getTimestamp("endDate"));
        Boolean active = doc.getBoolean("active");
        v.setActive(Boolean.TRUE.equals(active));
        return v;
    }

    /** Xây dựng Map để lưu lên Firestore với tên field chính xác */
    private Map<String, Object> toMap(@NonNull Voucher voucher) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", voucher.getCode());
        map.put("kind", voucher.getKind());         // field "kind" trong Firestore
        map.put("value", voucher.getValue());
        map.put("startDate", voucher.getStartDate());
        map.put("endDate", voucher.getEndDate());
        map.put("active", voucher.isActive());       // field "active" trong Firestore
        return map;
    }

    public LiveData<List<Voucher>> observeAllVouchers() {
        return new LiveData<List<Voucher>>() {
            private com.google.firebase.firestore.ListenerRegistration registration;

            @Override
            protected void onActive() {
                super.onActive();
                if (registration == null) {
                    registration = vouchersRef.addSnapshotListener((snapshots, error) -> {
                        if (error != null || snapshots == null) return;
                        List<Voucher> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Voucher v = fromDoc(doc);
                            if (v != null) list.add(v);
                        }
                        setValue(list);
                    });
                }
            }

            @Override
            protected void onInactive() {
                super.onInactive();
                if (registration != null) {
                    registration.remove();
                    registration = null;
                }
            }
        };
    }

    /** Lấy 1 lần — dùng khi mở AddEditVoucherActivity ở chế độ Sửa. */
    public LiveData<Voucher> getVoucherById(String voucherId) {
        MutableLiveData<Voucher> result = new MutableLiveData<>();
        vouchersRef.document(voucherId).get()
                .addOnSuccessListener(doc -> result.setValue(fromDoc(doc)));
        return result;
    }

    public void addVoucher(@NonNull Voucher voucher, FirebaseCallback<Void> callback) {
        String code = voucher.getCode().trim().toUpperCase();
        voucher.setCode(code);

        vouchersRef.whereEqualTo("code", code).get().addOnSuccessListener(snapshots -> {
            if (!snapshots.isEmpty()) {
                callback.onFailure(new Exception("Mã Voucher đã tồn tại"));
                return;
            }
            DocumentReference docRef = vouchersRef.document();
            voucher.setVoucherId(docRef.getId());
            docRef.set(toMap(voucher))
                    .addOnSuccessListener(unused -> callback.onSuccess(null))
                    .addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }

    public void updateVoucher(@NonNull Voucher voucher, FirebaseCallback<Void> callback) {
        String code = voucher.getCode().trim().toUpperCase();
        voucher.setCode(code);

        vouchersRef.whereEqualTo("code", code).get().addOnSuccessListener(snapshots -> {
            boolean duplicate = false;
            for (DocumentSnapshot doc : snapshots) {
                if (!doc.getId().equals(voucher.getVoucherId())) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                callback.onFailure(new Exception("Mã Voucher đã tồn tại"));
                return;
            }
            vouchersRef.document(voucher.getVoucherId()).set(toMap(voucher))
                    .addOnSuccessListener(unused -> callback.onSuccess(null))
                    .addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }

    /** Bật/tắt nhanh bằng switch trên item_admin_voucher */
    public void toggleActive(String voucherId, boolean isActive, FirebaseCallback<Void> callback) {
        vouchersRef.document(voucherId).update("active", isActive)  // dùng tên field "active"
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /** Xóa cứng voucher */
    public void deleteVoucher(String voucherId, FirebaseCallback<Void> callback) {
        vouchersRef.document(voucherId).delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /** Sắp xếp sắp hết hạn lên trước — endDate tăng dần. */
    public List<Voucher> sortByExpiringFirst(List<Voucher> source) {
        List<Voucher> sorted = new ArrayList<>(source);
        sorted.sort((a, b) -> {
            if (a.getEndDate() == null && b.getEndDate() == null) return 0;
            if (a.getEndDate() == null) return 1;
            if (b.getEndDate() == null) return -1;
            return a.getEndDate().compareTo(b.getEndDate());
        });
        return sorted;
    }

    /** Sắp xếp giá trị giảm cao nhất lên trước. */
    public List<Voucher> sortByValueDesc(List<Voucher> source) {
        List<Voucher> sorted = new ArrayList<>(source);
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return sorted;
    }

    public List<Voucher> filterByCode(List<Voucher> source, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return source;
        List<Voucher> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT).trim();
        for (Voucher v : source) {
            if (v.getCode() != null && v.getCode().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
                result.add(v);
            }
        }
        return result;
    }
}
