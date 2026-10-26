package com.example.bookapp.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Author;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DUY NHẤT được phép gọi Firestore cho collection "authors".
 * Dùng bởi BookDetailActivity để hiện tên tác giả (join qua book.getAuthorIds()) -
 * phần này trước đây đang để TODO/trống trong BookDetailActivity.
 */
public class AuthorRepository {

    public LiveData<Author> getAuthor(String authorId) {
        MutableLiveData<Author> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_AUTHORS)
                .document(authorId)
                .get()
                .addOnSuccessListener(doc -> {
                    Author author = doc.toObject(Author.class);
                    if (author != null) author.setAuthorId(doc.getId());
                    liveData.setValue(author);
                })
                .addOnFailureListener(e -> liveData.setValue(null));

        return liveData;
    }

    /**
     * Sách thường có nhiều authorId (book.getAuthorIds()), nên cần tải nhiều tác giả
     * cùng lúc. Firestore whereIn giới hạn tối đa 10 phần tử/lần gọi - với sách thông
     * thường (1-3 tác giả) không sao, nếu sau này cần nhiều hơn 10 phải tự chia batch.
     */
    public LiveData<List<Author>> getAuthorsByIds(List<String> authorIds) {
        MutableLiveData<List<Author>> liveData = new MutableLiveData<>();

        if (authorIds == null || authorIds.isEmpty()) {
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_AUTHORS)
                .whereIn("__name__", authorIds) // "__name__" = filter theo documentId
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Author> authors = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Author author = doc.toObject(Author.class);
                        author.setAuthorId(doc.getId());
                        authors.add(author);
                    });
                    liveData.setValue(authors);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    public LiveData<List<Author>> getAllAuthors() {
        MutableLiveData<List<Author>> liveData = new MutableLiveData<>();
        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_AUTHORS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Author> authors = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Author author = doc.toObject(Author.class);
                        author.setAuthorId(doc.getId());
                        authors.add(author);
                    });
                    liveData.setValue(authors);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));
        return liveData;
    }
}
