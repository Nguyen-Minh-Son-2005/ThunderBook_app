package com.example.bookapp.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Notification;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QuerySnapshot;
import android.util.Log;

/**
 * Lớp DUY NHẤT được phép gọi Firestore cho collection "notifications".
 * Dùng bởi NotificationActivity.
 */
public class NotificationRepository {

    public LiveData<List<Notification>> getNotifications(String uid) {
        MutableLiveData<List<Notification>> liveData = new MutableLiveData<>();

        Task<QuerySnapshot> personalTask = FirebaseUtils.getFirestore()
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", uid)
                .get();

        Task<QuerySnapshot> broadcastTask = FirebaseUtils.getFirestore()
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", null)
                .get();

        Tasks.whenAllSuccess(personalTask, broadcastTask).addOnSuccessListener(results -> {
            List<Notification> notifications = new ArrayList<>();
            for (Object result : results) {
                QuerySnapshot querySnapshot = (QuerySnapshot) result;
                querySnapshot.forEach(doc -> {
                    Notification notification = doc.toObject(Notification.class);
                    notification.setNotificationId(doc.getId());
                    notifications.add(notification);
                });
            }
            
            // Sort by createdAt DESCENDING
            Collections.sort(notifications, (n1, n2) -> {
                if (n1.getCreatedAt() == null || n2.getCreatedAt() == null) return 0;
                return n2.getCreatedAt().compareTo(n1.getCreatedAt());
            });
            
            liveData.setValue(notifications);
        }).addOnFailureListener(e -> {
            Log.e("NotificationRepository", "Error getting notifications", e);
            liveData.setValue(new ArrayList<>());
        });

        return liveData;
    }

    public void markAsRead(String notificationId, FirebaseCallback<Void> callback) {
        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /** Đánh dấu nhiều thông báo đã đọc cùng lúc - dùng cho nút "Đánh dấu tất cả đã đọc". */
    public void markAllAsRead(List<String> notificationIds, FirebaseCallback<Void> callback) {
        if (notificationIds.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        WriteBatch batch = FirebaseUtils.getFirestore().batch();
        for (String id : notificationIds) {
            batch.update(FirebaseUtils.getFirestore()
                    .collection(Constants.COLLECTION_NOTIFICATIONS).document(id), "isRead", true);
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}
