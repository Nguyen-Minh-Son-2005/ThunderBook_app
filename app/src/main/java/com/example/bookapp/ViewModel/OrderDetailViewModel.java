package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Order;
import com.example.bookapp.Repository.OrderRepository;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;

/**
 * ViewModel cho OrderDetailActivity.
 * Tách load + hủy đơn hàng ra khỏi Activity.
 */
public class OrderDetailViewModel extends ViewModel {

    private final OrderRepository orderRepository = new OrderRepository();

    private final MutableLiveData<Order> _order = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _cancelSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<Order> getOrder() { return _order; }
    public LiveData<Boolean> getCancelSuccess() { return _cancelSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    public void loadOrder(String orderId) {
        orderRepository.getOrder(orderId).observeForever(
                order -> _order.setValue(order));
    }

    /** Cập nhật trạng thái đơn hàng sang "cancelled". */
    public void cancelOrder(String orderId) {
        orderRepository.updateOrderStatus(orderId, "cancelled", new FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        _cancelSuccess.setValue(true);
                        // Reload để UI cập nhật trạng thái mới
                        loadOrder(orderId);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        _errorMessage.setValue(e.getMessage());
                    }
                });
    }
}
