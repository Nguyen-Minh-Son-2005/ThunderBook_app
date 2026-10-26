package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Notification;
import com.example.bookapp.Repository.NotificationRepository;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel cho NotificationActivity.
 * Tách load + markAsRead + markAllAsRead ra khỏi Activity.
 */
public class NotificationViewModel extends ViewModel {

    private final NotificationRepository notificationRepository = new NotificationRepository();

    private final MutableLiveData<List<Notification>> _notifications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    public LiveData<List<Notification>> getNotifications() { return _notifications; }
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    public void loadNotifications(String uid) {
        _isLoading.setValue(true);
        notificationRepository.getNotifications(uid).observeForever(notifications -> {
            _notifications.setValue(notifications);
            _isLoading.setValue(false);
        });
    }

    /** Đánh dấu 1 thông báo đã đọc. Cập nhật local state ngay lập tức, sync Firestore nền. */
    public void markAsRead(Notification notification) {
        notification.setRead(true);
        // Trigger lại notify bằng cách set lại cùng danh sách
        List<Notification> current = _notifications.getValue();
        if (current != null) _notifications.setValue(current);

        notificationRepository.markAsRead(notification.getNotificationId(),
                new FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) { /* đã cập nhật xong */ }

                    @Override
                    public void onFailure(Exception e) { /* không nghiêm trọng */ }
                });
    }

    /** Đánh dấu tất cả thông báo chưa đọc thành đã đọc — batch update. */
    public void markAllAsRead(List<Notification> notifications) {
        List<String> unreadIds = new ArrayList<>();
        for (Notification n : notifications) {
            if (!n.isRead()) {
                n.setRead(true);
                unreadIds.add(n.getNotificationId());
            }
        }
        if (unreadIds.isEmpty()) return;

        _notifications.setValue(notifications);

        notificationRepository.markAllAsRead(unreadIds, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) { /* batch đã xong */ }

            @Override
            public void onFailure(Exception e) { /* không nghiêm trọng */ }
        });
    }
}
