package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Notification;
import com.example.bookapp.Repository.AdminNotificationRepository;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;

import java.util.List;

/** Dùng cho ManageNotificationActivity. */
public class AdminNotificationViewModel extends ViewModel {

    private final AdminNotificationRepository repository = new AdminNotificationRepository();

    private final MutableLiveData<Boolean> sendSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<Notification>> loadRecentSent(int limit) {
        return repository.getRecentSentNotifications(limit);
    }

    public LiveData<Boolean> getSendSuccess() {
        return sendSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * targetUserId null = gửi broadcast cho TẤT CẢ user (ứng với chip_target_all).
     * type dùng Constants.NOTIF_TYPE_PROMO hoặc NOTIF_TYPE_SYSTEM tùy chip_type đang chọn.
     */
    public void send(String targetUserId, String title, String content, String type) {
        if (title == null || title.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập tiêu đề thông báo");
            return;
        }
        if (content == null || content.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập nội dung thông báo");
            return;
        }

        repository.sendNotification(targetUserId, title.trim(), content.trim(), type, null,
                new FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        sendSuccess.setValue(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        errorMessage.setValue(e.getMessage());
                    }
                });
    }

    public void sendToUserEmail(String email, String title, String content, String type) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập email người nhận");
            return;
        }
        FirebaseUtils.getFirestore().collection("users")
                .whereEqualTo("email", email.trim())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        errorMessage.setValue("Không tìm thấy người dùng với email này");
                    } else {
                        String targetUserId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        send(targetUserId, title, content, type);
                    }
                })
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }
}
