package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Review;
import com.example.bookapp.Repository.BookRepository;
import com.example.bookapp.Repository.ReviewRepository;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel cho MyReviewsActivity.
 * Tách load review + join tên sách + xóa ra khỏi Activity.
 */
public class MyReviewsViewModel extends ViewModel {

    private final ReviewRepository reviewRepository = new ReviewRepository();
    private final BookRepository bookRepository = new BookRepository();

    private final MutableLiveData<List<Review>> _reviews = new MutableLiveData<>();
    private final MutableLiveData<Map<String, String[]>> _bookInfoCache = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Integer> _deleteSuccessPosition = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<List<Review>> getReviews() { return _reviews; }
    public LiveData<Map<String, String[]>> getBookInfoCache() { return _bookInfoCache; }
    public LiveData<Integer> getDeleteSuccessPosition() { return _deleteSuccessPosition; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    public void loadReviews(String uid) {
        reviewRepository.getReviewsByUser(uid).observeForever(reviews -> {
            _reviews.setValue(reviews);
            if (reviews != null && !reviews.isEmpty()) {
                fetchBookInfo(reviews);
            }
        });
    }

    /**
     * Review model chỉ lưu bookId, cần join sang collection "books"
     * để lấy title + coverImageUrl để hiển thị.
     * Với số lượng review ít (thường vài chục), get() riêng lẻ là đủ nhanh.
     */
    private void fetchBookInfo(List<Review> reviews) {
        Map<String, String[]> cache = _bookInfoCache.getValue();
        if (cache == null) cache = new HashMap<>();
        final Map<String, String[]> finalCache = cache;

        int[] remaining = {reviews.size()};

        for (Review review : reviews) {
            if (finalCache.containsKey(review.getBookId())) {
                remaining[0]--;
                if (remaining[0] <= 0) _bookInfoCache.setValue(finalCache);
                continue;
            }

            bookRepository.getBook(review.getBookId()).observeForever(book -> {
                if (book != null) {
                    finalCache.put(review.getBookId(),
                            new String[]{book.getTitle(), book.getCoverImageUrl()});
                }
                remaining[0]--;
                if (remaining[0] <= 0) _bookInfoCache.setValue(finalCache);
            });
        }
    }

    public void deleteReview(Review review, int position) {
        reviewRepository.deleteReview(review.getReviewId(), new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<Review> current = _reviews.getValue();
                if (current != null) {
                    current.remove(review);
                    _reviews.setValue(current);
                }
                _deleteSuccessPosition.setValue(position);
            }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.setValue(e.getMessage());
            }
        });
    }
}
