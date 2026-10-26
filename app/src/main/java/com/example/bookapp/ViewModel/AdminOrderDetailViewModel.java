package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Order;
import com.example.bookapp.Repository.AdminNotificationRepository;
import com.example.bookapp.Repository.AdminOrderRepository;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;

/** Dùng cho AdminOrderDetailActivity. */
public class AdminOrderDetailViewModel extends ViewModel {

    private final AdminOrderRepository orderRepository = new AdminOrderRepository();
    private final AdminNotificationRepository notificationRepository = new AdminNotificationRepository();

    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /** Nhãn hiển thị tiếng Việt tương ứng từng trạng thái — dùng cho cả UI lẫn nội dung thông báo gửi user. */
    private static final String[][] STATUS_LABELS = {
            {Constants.ORDER_PENDING, "Chờ xử lý"},
            {Constants.ORDER_CONFIRMED, "Đã xác nhận"},
            {Constants.ORDER_PACKING, "Đang đóng gói"},
            {Constants.ORDER_SHIPPING, "Đang giao hàng"},
            {Constants.ORDER_DELIVERED, "Đã giao hàng"},
            {Constants.ORDER_CANCELLED, "Đã hủy"},
            {Constants.ORDER_RETURNED, "Đã trả hàng"},
    };

    public LiveData<Order> loadOrder(String orderId) {
        return orderRepository.getOrderById(orderId);
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /** Thứ tự các bước tiếp theo hợp lệ — Activity dùng để tự sinh nhãn nút "Xác nhận đơn → Đang đóng gói". */
    public String getNextStatus(String currentStatus) {
        switch (currentStatus) {
            case Constants.ORDER_PENDING:
                return Constants.ORDER_CONFIRMED;
            case Constants.ORDER_CONFIRMED:
                return Constants.ORDER_PACKING;
            case Constants.ORDER_PACKING:
                return Constants.ORDER_SHIPPING;
            case Constants.ORDER_SHIPPING:
                return Constants.ORDER_DELIVERED;
            default:
                return null; // đã delivered/cancelled/returned — không còn bước tiếp theo
        }
    }

    public String getStatusLabel(String status) {
        for (String[] pair : STATUS_LABELS) {
            if (pair[0].equals(status)) return pair[1];
        }
        return status;
    }

    /**
     * Đổi trạng thái đơn hàng — SAU KHI cập nhật thành công mới gọi tiếp gửi thông báo
     * cho user, đúng nguyên tắc đã thống nhất khi thiết kế AdminOrderRepository (không
     * tự gửi thông báo bên trong Repository).
     */
    public void updateStatus(String orderId, String userId, String newStatus) {
        orderRepository.updateOrderStatus(orderId, newStatus, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateSuccess.setValue(true);
                notificationRepository.sendOrderStatusNotification(
                        userId, orderId, getStatusLabel(newStatus),
                        new FirebaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                // Gửi thông báo thành công — không cần báo gì thêm lên UI,
                                // updateSuccess đã setValue(true) ở bước đổi trạng thái rồi.
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Đơn hàng đã đổi trạng thái thành công dù gửi thông báo lỗi —
                                // không rollback, chỉ log lỗi, không chặn luồng chính của admin.
                            }
                        });
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }

    public void cancelOrder(String orderId, String userId, String currentStatus) {
        if (Constants.ORDER_DELIVERED.equals(currentStatus) || Constants.ORDER_SHIPPING.equals(currentStatus)) {
            errorMessage.setValue("Không thể hủy đơn hàng đang giao hoặc đã giao");
            return;
        }
        orderRepository.cancelOrder(orderId, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateSuccess.setValue(true);
                notificationRepository.sendOrderStatusNotification(
                        userId, orderId, "Đã hủy", new FirebaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                            }

                            @Override
                            public void onFailure(Exception e) {
                            }
                        });
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }
}
