package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Order;
import com.example.bookapp.Model.User;
import com.example.bookapp.Repository.AdminOrderRepository;
import com.example.bookapp.Repository.AdminUserRepository;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.List;

/** Dùng cho màn chi tiết 1 user phía Admin — xem thông tin, lịch sử mua, đổi quyền. */
public class AdminUserDetailViewModel extends ViewModel {

    private final AdminUserRepository userRepository = new AdminUserRepository();
    private final AdminOrderRepository orderRepository = new AdminOrderRepository();

    private final MutableLiveData<Boolean> roleUpdateSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<User> loadUser(String userId) {
        return userRepository.getUserById(userId);
    }

    /** Lịch sử đơn hàng của user này — dùng cho rv_user_orders trong màn chi tiết. */
    public LiveData<List<Order>> loadOrderHistory(String userId) {
        return orderRepository.getOrdersByUserId(userId);
    }

    public LiveData<Boolean> getRoleUpdateSuccess() {
        return roleUpdateSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Đổi quyền admin/user — Activity PHẢI hiện dialog xác nhận trước khi gọi hàm này
     * (đúng lưu ý đã ghi trong AdminUserRepository, đây là thao tác nhạy cảm).
     */
    public void setAdminRole(String userId, boolean grantAdmin) {
        String newRole = grantAdmin ? Constants.ROLE_ADMIN : Constants.ROLE_USER;
        userRepository.updateUserRole(userId, newRole, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                roleUpdateSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }
}
