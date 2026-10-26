package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Order;
import com.example.bookapp.Repository.AdminOrderRepository;

import java.util.ArrayList;
import java.util.List;

/** Dùng cho ManageOrderActivity — danh sách + Tab trạng thái + tìm kiếm + sắp xếp. */
public class AdminOrderViewModel extends ViewModel {

    public enum SortOption {NEWEST, OLDEST, HIGHEST_VALUE}

    private final AdminOrderRepository repository = new AdminOrderRepository();

    private final LiveData<List<Order>> allOrders = repository.observeAllOrders();
    private final MediatorLiveData<List<Order>> displayedOrders = new MediatorLiveData<>();
    private List<Order> cachedList = new ArrayList<>();

    private String currentKeyword = "";
    private String currentStatus = null; // null = Tab "Tất cả"
    private SortOption currentSort = SortOption.NEWEST;

    public AdminOrderViewModel() {
        displayedOrders.addSource(allOrders, list -> {
            cachedList = list != null ? list : new ArrayList<>();
            applyFilterSort();
        });
    }

    public LiveData<List<Order>> getDisplayedOrders() {
        return displayedOrders;
    }

    public void search(String keyword) {
        currentKeyword = keyword;
        applyFilterSort();
    }

    /** status null ứng với Tab "Tất cả" — dùng hằng số Constants.ORDER_* cho các tab còn lại. */
    public void setStatusFilter(String status) {
        currentStatus = status;
        applyFilterSort();
    }

    public void setSortOption(SortOption option) {
        currentSort = option;
        applyFilterSort();
    }

    private void applyFilterSort() {
        List<Order> result = repository.filterByKeyword(cachedList, currentKeyword);
        result = repository.filterByStatus(result, currentStatus);

        List<Order> sorted = new ArrayList<>(result);
        switch (currentSort) {
            case OLDEST:
                sorted.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
                break;
            case HIGHEST_VALUE:
                sorted.sort((a, b) -> Double.compare(b.getFinalTotal(), a.getFinalTotal()));
                break;
            case NEWEST:
            default:
                // Đã orderBy createdAt DESC sẵn từ Repository, giữ nguyên thứ tự.
                break;
        }

        displayedOrders.setValue(sorted);
    }
}
