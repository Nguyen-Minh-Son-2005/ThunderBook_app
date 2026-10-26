package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Author;
import com.example.bookapp.Repository.AdminAuthorRepository;
import com.example.bookapp.Repository.AdminBookRepository;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.ArrayList;
import java.util.List;

/** Dùng cho ManageAuthorActivity. */
public class AdminAuthorViewModel extends ViewModel {

    private final AdminAuthorRepository repository = new AdminAuthorRepository();
    private final AdminBookRepository bookRepository = new AdminBookRepository();

    private final LiveData<List<Author>> allAuthors = repository.observeAllAuthors();
    private final MediatorLiveData<List<Author>> displayedAuthors = new MediatorLiveData<>();
    private List<Author> cachedList = new ArrayList<>();
    private String currentKeyword = "";
    private boolean sortByBookCountEnabled = false;

    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminAuthorViewModel() {
        displayedAuthors.addSource(allAuthors, list -> {
            cachedList = list != null ? list : new ArrayList<>();
            applyFilter();
        });
    }

    public LiveData<List<Author>> getDisplayedAuthors() {
        return displayedAuthors;
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

    /** Gọi khi bấm nút sắp xếp, chọn tiêu chí "Số sách nhiều nhất" (BottomSheet/PopupMenu). */
    public void setSortByBookCount(boolean enabled) {
        sortByBookCountEnabled = enabled;
        applyFilter();
    }

    private void applyFilter() {
        List<Author> filtered = repository.filterByName(cachedList, currentKeyword);
        if (sortByBookCountEnabled) {
            filtered = repository.sortByBookCount(filtered);
        }
        displayedAuthors.setValue(filtered);
    }

    /** authorId null = thêm mới, khác null = sửa — đúng logic dialog_add_edit_simple_entity dùng chung. */
    public void saveAuthor(String authorId, String name, String imageUrl) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập tên tác giả");
            return;
        }
        Author author = new Author();
        author.setAuthorId(authorId);
        author.setName(name.trim());
        author.setAvatarUrl(imageUrl);

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

        if (authorId == null) {
            repository.addAuthor(author, callback);
        } else {
            repository.updateAuthor(author, callback);
        }
    }

    public void deleteAuthor(String authorId) {
        bookRepository.checkAuthorInUse(authorId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean inUse) {
                if (inUse) {
                    errorMessage.setValue("Không thể xóa vì đang có sản phẩm thuộc Tác giả này.");
                } else {
                    repository.deleteAuthor(authorId, new FirebaseCallback<Void>() {
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
