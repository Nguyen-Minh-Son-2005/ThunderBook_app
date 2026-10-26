package com.example.bookapp.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.bookapp.Model.Category;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCategoryRepository {

    private final CollectionReference categoriesRef =
            FirebaseUtils.getFirestore().collection(Constants.COLLECTION_CATEGORIES);

    /** Realtime toàn bộ thể loại, A-Z — dùng cho ManageCategoryActivity và dropdown chọn thể loại. */
    public LiveData<List<Category>> observeAllCategories() {
        Query query = categoriesRef.orderBy(Constants.FIELD_NAME, Query.Direction.ASCENDING);
        return new FirestoreListLiveData<>(query, Category.class);
    }

    public void addCategory(@NonNull Category category, FirebaseCallback<Void> callback) {
        DocumentReference docRef = categoriesRef.document();
        category.setCategoryId(docRef.getId());
        category.setActive(true);
        docRef.set(category)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateCategory(@NonNull Category category, FirebaseCallback<Void> callback) {
        categoriesRef.document(category.getCategoryId()).set(category)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Xóa thể loại — CHO PHÉP xóa cứng vì đây là dữ liệu tham chiếu (categoryIds trong
     * books là mảng id), khác với Book là dữ liệu snapshot trong đơn hàng cũ. Cân nhắc:
     * nên kiểm tra không còn sách nào dùng categoryId này trước khi xóa (làm ở ViewModel).
     */
    public void deleteCategory(String categoryId, FirebaseCallback<Void> callback) {
        categoriesRef.document(categoryId).delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public List<Category> filterByName(List<Category> source, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return source;
        List<Category> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT).trim();
        for (Category c : source) {
            if (c.getName() != null && c.getName().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
                result.add(c);
            }
        }
        return result;
    }
}
