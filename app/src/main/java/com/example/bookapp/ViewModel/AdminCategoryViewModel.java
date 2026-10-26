package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Category;
import com.example.bookapp.Repository.AdminBookRepository;
import com.example.bookapp.Repository.AdminCategoryRepository;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.ArrayList;
import java.util.List;

/** Dùng cho ManageCategoryActivity. */
public class AdminCategoryViewModel extends ViewModel {

    private final AdminCategoryRepository repository = new AdminCategoryRepository();
    private final AdminBookRepository bookRepository = new AdminBookRepository();

    private final LiveData<List<Category>> allCategories = repository.observeAllCategories();
    private final MediatorLiveData<List<Category>> displayedCategories = new MediatorLiveData<>();
    private List<Category> cachedList = new ArrayList<>();
    private String currentKeyword = "";

    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminCategoryViewModel() {
        displayedCategories.addSource(allCategories, list -> {
            cachedList = list != null ? list : new ArrayList<>();
            applyFilter();
        });
    }

    public LiveData<List<Category>> getDisplayedCategories() {
        return displayedCategories;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /** Gọi mỗi khi người dùng gõ vào et_search (activity dùng TextWatcher). */
    public void search(String keyword) {
        currentKeyword = keyword;
        applyFilter();
    }

    private void applyFilter() {
        displayedCategories.setValue(repository.filterByName(cachedList, currentKeyword));
    }

    /** categoryId null = thêm mới, khác null = sửa — đúng logic dialog_add_edit_simple_entity dùng chung. */
    public void saveCategory(String categoryId, String name, String imageUrl) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập tên thể loại");
            return;
        }
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName(name.trim());
        category.setImageUrl(imageUrl);

        FirebaseCallback<Void> callback = new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                saveSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        };

        if (categoryId == null) {
            repository.addCategory(category, callback);
        } else {
            repository.updateCategory(category, callback);
        }
    }

    public void deleteCategory(String categoryId) {
        bookRepository.checkCategoryInUse(categoryId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean inUse) {
                if (inUse) {
                    errorMessage.setValue("Không thể xóa vì đang có sản phẩm thuộc Thể loại này.");
                } else {
                    repository.deleteCategory(categoryId, new FirebaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            saveSuccess.setValue(true);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            errorMessage.setValue(e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }
}
