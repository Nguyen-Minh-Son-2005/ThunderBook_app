package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Publisher;
import com.example.bookapp.Repository.AdminBookRepository;
import com.example.bookapp.Repository.AdminPublisherRepository;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.ArrayList;
import java.util.List;

/** Dùng cho ManagePublisherActivity. */
public class AdminPublisherViewModel extends ViewModel {

    private final AdminPublisherRepository repository = new AdminPublisherRepository();
    private final AdminBookRepository bookRepository = new AdminBookRepository();

    private final LiveData<List<Publisher>> allPublishers = repository.observeAllPublishers();
    private final MediatorLiveData<List<Publisher>> displayedPublishers = new MediatorLiveData<>();
    private List<Publisher> cachedList = new ArrayList<>();
    private String currentKeyword = "";

    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminPublisherViewModel() {
        displayedPublishers.addSource(allPublishers, list -> {
            cachedList = list != null ? list : new ArrayList<>();
            applyFilter();
        });
    }

    public LiveData<List<Publisher>> getDisplayedPublishers() {
        return displayedPublishers;
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

    // Không có setSortByBookCount() ở đây — khác với AdminAuthorViewModel, vì Publisher
    // không có field bookCount (xem ghi chú trong AdminPublisherRepository). Nếu sau này
    // cần thêm sắp xếp cho NXB, dùng tiêu chí khác (vd A-Z, mới thêm gần đây) thay vì
    // "số sách nhiều nhất".

    private void applyFilter() {
        displayedPublishers.setValue(repository.filterByName(cachedList, currentKeyword));
    }

    /** publisherId null = thêm mới, khác null = sửa — đúng logic dialog_add_edit_simple_entity dùng chung. */
    public void savePublisher(String publisherId, String name, String imageUrl) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập tên nhà xuất bản");
            return;
        }
        
        String trimmedName = name.trim();
        for (Publisher p : cachedList) {
            if (p.getName().equalsIgnoreCase(trimmedName)) {
                if (publisherId == null || !p.getPublisherId().equals(publisherId)) {
                    errorMessage.setValue("Tên nhà xuất bản đã tồn tại");
                    return;
                }
            }
        }
        
        Publisher publisher = new Publisher();
        publisher.setPublisherId(publisherId);
        publisher.setName(name.trim());
        publisher.setLogoUrl(imageUrl);

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

        if (publisherId == null) {
            repository.addPublisher(publisher, callback);
        } else {
            repository.updatePublisher(publisher, callback);
        }
    }

    public void deletePublisher(String publisherId) {
        bookRepository.checkPublisherInUse(publisherId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean inUse) {
                if (inUse) {
                    errorMessage.setValue("Không thể xóa vì đang có sản phẩm thuộc Nhà xuất bản này.");
                } else {
                    repository.deletePublisher(publisherId, new FirebaseCallback<Void>() {
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
