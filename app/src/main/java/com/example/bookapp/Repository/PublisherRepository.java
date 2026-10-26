package com.example.bookapp.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Publisher;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseUtils;

/**
 * Lớp DUY NHẤT được phép gọi Firestore cho collection "publishers".
 * Dùng bởi BookDetailActivity để hiện tên NXB (join qua book.getPublisherId()) -
 * phần này trước đây đang để TODO/trống trong BookDetailActivity.
 */
public class PublisherRepository {

    public LiveData<Publisher> getPublisher(String publisherId) {
        MutableLiveData<Publisher> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_PUBLISHERS)
                .document(publisherId)
                .get()
                .addOnSuccessListener(doc -> {
                    Publisher publisher = doc.toObject(Publisher.class);
                    if (publisher != null) publisher.setPublisherId(doc.getId());
                    liveData.setValue(publisher);
                })
                .addOnFailureListener(e -> liveData.setValue(null));

        return liveData;
    }
}
