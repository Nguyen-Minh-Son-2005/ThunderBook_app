package com.example.bookapp.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Utils.FirebaseUtils;

import java.util.HashSet;
import java.util.Set;

public class FavoriteRepository {
    private static final MutableLiveData<Set<String>> favoriteBookIds = new MutableLiveData<>(new HashSet<>());
    private static com.google.firebase.firestore.ListenerRegistration listenerRegistration;

    public static LiveData<Set<String>> getFavoriteBookIds() {
        return favoriteBookIds;
    }

    public static void loadFavorites() {
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid == null) return;
        
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = FirebaseUtils.getFirestore().collection("users")
                .document(uid).collection("favorites")
                .addSnapshotListener((qs, e) -> {
                    if (qs != null) {
                        Set<String> ids = new HashSet<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : qs) {
                            ids.add(doc.getId());
                        }
                        favoriteBookIds.setValue(ids);
                    }
                });
    }

    public static void toggleFavorite(String bookId, boolean isCurrentlyFavorite) {
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid == null) return;

        if (isCurrentlyFavorite) {
            FirebaseUtils.getFirestore().collection("users").document(uid)
                    .collection("favorites").document(bookId).delete();
        } else {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("timestamp", com.google.firebase.Timestamp.now());
            FirebaseUtils.getFirestore().collection("users").document(uid)
                    .collection("favorites").document(bookId).set(data);
        }
    }
}
