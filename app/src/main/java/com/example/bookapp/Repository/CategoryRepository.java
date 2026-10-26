package com.example.bookapp.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Category;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DUY NHẤT được phép gọi Firestore cho collection "categories".
 * Dùng bởi HomeFragment, SearchActivity.
 */
public class CategoryRepository {

    public LiveData<List<Category>> getActiveCategories() {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_CATEGORIES)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Category> categories = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Category category = doc.toObject(Category.class);
                        category.setCategoryId(doc.getId());
                        categories.add(category);
                    });
                    liveData.setValue(categories);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    public LiveData<List<Category>> getCategoriesByIds(List<String> categoryIds) {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();

        if (categoryIds == null || categoryIds.isEmpty()) {
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_CATEGORIES)
                .whereIn("__name__", categoryIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Category> categories = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Category category = doc.toObject(Category.class);
                        category.setCategoryId(doc.getId());
                        categories.add(category);
                    });
                    liveData.setValue(categories);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }
}
