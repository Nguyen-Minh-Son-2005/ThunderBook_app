package com.example.bookapp.Repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Notification;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.List;
import com.google.firebase.Timestamp;
public class AdminNotificationRepository {

    private final CollectionReference notificationsRef =
            FirebaseUtils.getFirestore().collection(Constants.COLLECTION_NOTIFICATIONS);

    /**
     * Gửi thông báo — nếu userId = null thì đây là broadcast (hiện cho TẤT CẢ user).
     * Dùng cho ManageNotificationActivity.
     */
    public void sendNotification(@Nullable String userId, String title, String content,
                                  String type, @Nullable String relatedId, FirebaseCallback<Void> callback) {
        DocumentReference docRef = notificationsRef.document();
        Notification notification = new Notification();
        notification.setNotificationId(docRef.getId());
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setRead(false);
        notification.setCreatedAt(Timestamp.now());

        docRef.set(notification)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Gửi thông báo tự động khi admin đổi trạng thái đơn hàng (gọi tiếp theo sau khi
     * AdminOrderRepository.updateOrderStatus() thành công) — type = "order" để user phân
     * biệt với thông báo khuyến mãi. Dùng đúng tên hằng số NOTIF_TYPE_ORDER User đã đặt
     * trong Constants.java, không phải NOTI_ORDER.
     */
    public void sendOrderStatusNotification(String userId, String orderId, String statusLabel,
                                             FirebaseCallback<Void> callback) {
        String title = "Cập nhật đơn hàng #" + orderId;
        String content = "Đơn hàng của bạn hiện đang: " + statusLabel;
        sendNotification(userId, title, content, Constants.NOTIF_TYPE_ORDER, orderId, callback);
    }

    /** N thông báo admin đã gửi gần đây, lấy 1 lần — khối "Đã gửi gần đây" trong ManageNotificationActivity. */
    public LiveData<List<Notification>> getRecentSentNotifications(int limit) {
        MutableLiveData<List<Notification>> result = new MutableLiveData<>();
        notificationsRef.orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(snapshots -> result.setValue(snapshots.toObjects(Notification.class)));
        return result;
    }
}
