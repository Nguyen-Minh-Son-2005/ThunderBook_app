package com.example.bookapp.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.bookapp.Model.Publisher;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminPublisherRepository {

    private final CollectionReference publishersRef =
            FirebaseUtils.getFirestore().collection(Constants.COLLECTION_PUBLISHERS);

    /** Realtime toàn bộ nhà xuất bản, A-Z — dùng cho ManagePublisherActivity và dropdown chọn nhà xuất bản. */
    public LiveData<List<Publisher>> observeAllPublishers() {
        Query query = publishersRef.orderBy(Constants.FIELD_NAME, Query.Direction.ASCENDING);
        return new FirestoreListLiveData<>(query, Publisher.class);
    }

    public void addPublisher(@NonNull Publisher publisher, FirebaseCallback<Void> callback) {
        DocumentReference docRef = publishersRef.document();
        publisher.setPublisherId(docRef.getId());
        docRef.set(publisher)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void updatePublisher(@NonNull Publisher publisher, FirebaseCallback<Void> callback) {
        publishersRef.document(publisher.getPublisherId()).set(publisher)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void deletePublisher(String publisherId, FirebaseCallback<Void> callback) {
        publishersRef.document(publisherId).delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // Không có sortByBookCount() ở đây — khác với Author, Publisher KHÔNG có field
    // bookCount trong thiết kế Firestore gốc (xem database-schema-app-ban-sach.md,
    // mục "5. publishers"). Nếu sau này cần đếm số sách theo NXB, phải đếm bằng cách
    // query books.whereEqualTo("publisherId", id) rồi .size() — không denormalize sẵn.

    public List<Publisher> filterByName(List<Publisher> source, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return source;
        List<Publisher> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT).trim();
        for (Publisher a : source) {
            if (a.getName() != null && a.getName().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
                result.add(a);
            }
        }
        return result;
    }
}
