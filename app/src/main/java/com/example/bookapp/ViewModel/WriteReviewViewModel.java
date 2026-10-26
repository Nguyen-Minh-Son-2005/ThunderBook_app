package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.Review;
import com.example.bookapp.Model.User;
import com.example.bookapp.Repository.BookRepository;
import com.example.bookapp.Repository.ReviewRepository;
import com.example.bookapp.Repository.UserRepository;
import com.example.bookapp.Utils.FirebaseCallback;
import com.google.firebase.Timestamp;

import java.util.UUID;

/**
 * ViewModel cho WriteReviewActivity.
 * Tách 3 lần gọi Firestore (load sách + load user + gửi review) ra khỏi Activity.
 */
public class WriteReviewViewModel extends ViewModel {

    private final ReviewRepository reviewRepository = new ReviewRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final UserRepository userRepository = new UserRepository();

    private final MutableLiveData<Book> _book = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _submitSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<Book> getBook() { return _book; }
    public LiveData<Boolean> getSubmitSuccess() { return _submitSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    private LiveData<Book> bookLiveData;
    private androidx.lifecycle.Observer<Book> bookObserver;

    public void loadBook(String bookId) {
        if (bookLiveData != null && bookObserver != null) {
            bookLiveData.removeObserver(bookObserver);
        }
        bookLiveData = bookRepository.getBook(bookId);
        bookObserver = book -> _book.setValue(book);
        bookLiveData.observeForever(bookObserver);
    }

    /**
     * Gửi đánh giá: load thông tin user trước (lấy fullName + avatarUrl để lưu snapshot),
     * sau đó tạo document review.
     */
    public void submitReview(String uid, String bookId, int rating,
                             String comment, String orderId) {
        LiveData<User> userLiveData = userRepository.getUser(uid);
        androidx.lifecycle.Observer<User>[] userObserver = new androidx.lifecycle.Observer[1];
        userObserver[0] = user -> {
            userLiveData.removeObserver(userObserver[0]);
            if (user == null) {
                _errorMessage.setValue("Không lấy được thông tin người dùng");
                return;
            }

            String reviewId = UUID.randomUUID().toString();
            Review review = new Review(
                    reviewId,
                    bookId,
                    uid,
                    user.getFullName(),
                    user.getAvatarUrl(),
                    rating,
                    comment,
                    orderId,
                    Timestamp.now()
            );

            reviewRepository.addReview(review, new FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    _submitSuccess.setValue(true);
                }

                @Override
                public void onFailure(Exception e) {
                    _errorMessage.setValue(e.getMessage());
                }
            });
        };
        userLiveData.observeForever(userObserver[0]);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (bookLiveData != null && bookObserver != null) {
            bookLiveData.removeObserver(bookObserver);
        }
    }
}
