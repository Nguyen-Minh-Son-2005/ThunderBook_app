package com.example.bookapp.ViewModel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Order;
import com.example.bookapp.Repository.OrderRepository;

import java.util.List;

/**
 * ViewModel cho OrderHistoryActivity.
 * Tab chỉ gọi loadOrders(status) — không tự truy vấn Firestore.
 */
public class OrderHistoryViewModel extends ViewModel {

    private final OrderRepository orderRepository = new OrderRepository();

    private final MutableLiveData<List<Order>> _orders = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    public LiveData<List<Order>> getOrders() { return _orders; }
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    /**
     * Tải danh sách đơn hàng theo trạng thái.
     * statusFilter = null → lấy tất cả đơn của user.
     */
    public void loadOrders(String uid, @Nullable String statusFilter) {
        _isLoading.setValue(true);
        orderRepository.getOrdersByUser(uid, statusFilter).observeForever(orders -> {
            _orders.setValue(orders);
            _isLoading.setValue(false);
        });
    }

    public void cancelOrder(String orderId, com.example.bookapp.Utils.FirebaseCallback<Void> callback) {
        orderRepository.updateOrderStatus(orderId, "cancelled", callback);
    }
}
