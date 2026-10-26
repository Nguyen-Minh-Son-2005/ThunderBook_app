package com.example.bookapp.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.bookapp.Model.Author;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminAuthorRepository {

    private final CollectionReference authorsRef =
            FirebaseUtils.getFirestore().collection(Constants.COLLECTION_AUTHORS);

    /** Realtime toàn bộ tác giả, A-Z — dùng cho ManageAuthorActivity và dropdown chọn tác giả. */
    public LiveData<List<Author>> observeAllAuthors() {
        Query query = authorsRef.orderBy(Constants.FIELD_NAME, Query.Direction.ASCENDING);
        return new FirestoreListLiveData<>(query, Author.class);
    }

    public void addAuthor(@NonNull Author author, FirebaseCallback<Void> callback) {
        DocumentReference docRef = authorsRef.document();
        author.setAuthorId(docRef.getId());
        author.setBookCount(0);
        docRef.set(author)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateAuthor(@NonNull Author author, FirebaseCallback<Void> callback) {
        authorsRef.document(author.getAuthorId()).set(author)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteAuthor(String authorId, FirebaseCallback<Void> callback) {
        authorsRef.document(authorId).delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /** Sắp xếp theo số đầu sách nhiều nhất — 1 trong 2 tiêu chí sắp xếp của ManageAuthorActivity. */
    public List<Author> sortByBookCount(List<Author> source) {
        List<Author> sorted = new ArrayList<>(source);
        sorted.sort((a, b) -> Integer.compare(b.getBookCount(), a.getBookCount()));
        return sorted;
    }

    public List<Author> filterByName(List<Author> source, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return source;
        List<Author> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT).trim();
        for (Author a : source) {
            if (a.getName() != null && a.getName().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
                result.add(a);
            }
        }
        return result;
    }
}
