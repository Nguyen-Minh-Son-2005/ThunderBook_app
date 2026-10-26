package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Review;
import com.example.bookapp.Repository.OrderRepository;
import com.example.bookapp.Repository.ReviewRepository;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;

import java.util.List;

public class BookReviewsViewModel extends ViewModel {
    private final ReviewRepository reviewRepository = new ReviewRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    
    private final MutableLiveData<List<Review>> _reviews = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _canReview = new MutableLiveData<>(false);

    public LiveData<List<Review>> getReviews() {
        return _reviews;
    }

    public LiveData<Boolean> getCanReview() {
        return _canReview;
    }

    public void loadReviews(String bookId) {
        reviewRepository.getAllReviewsByBook(bookId).observeForever(reviews -> {
            _reviews.setValue(reviews);
        });
    }

    public void checkCanReview(String bookId) {
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid == null) {
            _canReview.setValue(false);
            return;
        }
        orderRepository.checkUserPurchasedBook(uid, bookId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                _canReview.setValue(result != null && result);
            }
            @Override
            public void onFailure(Exception e) {
                _canReview.setValue(false);
            }
        });
    }
}
