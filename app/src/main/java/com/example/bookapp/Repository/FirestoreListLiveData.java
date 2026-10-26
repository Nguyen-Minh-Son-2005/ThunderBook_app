package com.example.bookapp.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.List;

/**
 * LiveData tự lắng nghe Firestore realtime CHỈ KHI có ít nhất 1 Observer đang active
 * (Activity ở foreground) — tự động removeListener() khi không còn ai quan sát nữa.
 * Đây là cách Google khuyến nghị khi kết hợp Firestore snapshot listener với LiveData,
 * giúp ViewModel không cần tự cầm ListenerRegistration rồi nhớ gọi remove() ở onCleared().
 * Dùng chung cho mọi Repository cần "observeAllXxx()" — chỉ cần truyền vào Query đã
 * build sẵn (orderBy/whereEqualTo...) và Model class tương ứng.
 */
public class FirestoreListLiveData<T> extends LiveData<List<T>> {

    private final Query query;
    private final Class<T> modelClass;
    private ListenerRegistration registration;

    public FirestoreListLiveData(@NonNull Query query, @NonNull Class<T> modelClass) {
        this.query = query;
        this.modelClass = modelClass;
    }

    @Override
    protected void onActive() {
        super.onActive();
        if (registration == null) {
            registration = query.addSnapshotListener((snapshots, error) -> {
                if (error != null || snapshots == null) return;
                setValue(snapshots.toObjects(modelClass));
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
}
