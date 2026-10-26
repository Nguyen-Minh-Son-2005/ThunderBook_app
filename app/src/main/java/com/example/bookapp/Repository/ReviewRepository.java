package com.example.bookapp.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Review;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lớp DUY NHẤT được phép gọi Firestore cho collection "reviews".
 * Dùng bởi WriteReviewActivity (viết mới), MyReviewsActivity (xem lại + xóa).
 */
public class ReviewRepository {

    public void addReview(Review review, FirebaseCallback<Void> callback) {
        String reviewId = review.getUserId() + "_" + review.getBookId() + "_" + review.getOrderId();
        review.setReviewId(reviewId);

        com.google.firebase.firestore.FirebaseFirestore db = FirebaseUtils.getFirestore();
        com.google.firebase.firestore.DocumentReference bookRef = db.collection(Constants.COLLECTION_BOOKS).document(review.getBookId());
        com.google.firebase.firestore.DocumentReference reviewRef = db.collection(Constants.COLLECTION_REVIEWS).document(reviewId);

        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot reviewSnap = transaction.get(reviewRef);
            if (reviewSnap.exists()) {
                throw new com.google.firebase.firestore.FirebaseFirestoreException(
                        "Bạn đã đánh giá sản phẩm này trong đơn hàng này rồi.",
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.ALREADY_EXISTS);
            }

            com.google.firebase.firestore.DocumentSnapshot bookSnap = transaction.get(bookRef);
            if (bookSnap.exists()) {
                double currentRating = bookSnap.getDouble("rating") != null ? bookSnap.getDouble("rating") : 0;
                long currentCount = bookSnap.getLong("ratingCount") != null ? bookSnap.getLong("ratingCount") : 0;

                double newRating = ((currentRating * currentCount) + review.getRating()) / (currentCount + 1);

                transaction.update(bookRef, "rating", newRating);
                transaction.update(bookRef, "ratingCount", currentCount + 1);
            }
            transaction.set(reviewRef, review);
            return null;
        }).addOnSuccessListener(unused -> callback.onSuccess(null))
          .addOnFailureListener(callback::onFailure);
    }

    public LiveData<List<Review>> getReviewsByUser(String uid) {
        MutableLiveData<List<Review>> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_REVIEWS)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Review review = doc.toObject(Review.class);
                        review.setReviewId(doc.getId());
                        reviews.add(review);
                    });
                    reviews.sort((r1, r2) -> {
                        if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                        if (r1.getCreatedAt() == null) return 1;
                        if (r2.getCreatedAt() == null) return -1;
                        return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                    });
                    liveData.setValue(reviews);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    /** Review preview cho BookDetailActivity - lấy vài review mới nhất của 1 cuốn sách. */
    public LiveData<List<Review>> getReviewsByBook(String bookId, int limit) {
        MutableLiveData<List<Review>> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_REVIEWS)
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Review review = doc.toObject(Review.class);
                        review.setReviewId(doc.getId());
                        reviews.add(review);
                    });
                    // Sắp xếp giảm dần theo thời gian tạo (khắc phục lỗi thiếu composite index)
                    reviews.sort((r1, r2) -> {
                        if (r1.getCreatedAt() == null || r2.getCreatedAt() == null) return 0;
                        return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                    });
                    List<Review> finalReviews = reviews;
                    if (finalReviews.size() > limit) {
                        finalReviews = finalReviews.subList(0, limit);
                    }
                    liveData.setValue(finalReviews);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    public LiveData<List<Review>> getAllReviewsByBook(String bookId) {
        MutableLiveData<List<Review>> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_REVIEWS)
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Review review = doc.toObject(Review.class);
                        review.setReviewId(doc.getId());
                        reviews.add(review);
                    });
                    // Sắp xếp giảm dần theo thời gian tạo (khắc phục lỗi thiếu composite index)
                    reviews.sort((r1, r2) -> {
                        if (r1.getCreatedAt() == null || r2.getCreatedAt() == null) return 0;
                        return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                    });
                    liveData.setValue(reviews);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    public void deleteReview(String reviewId, FirebaseCallback<Void> callback) {
        com.google.firebase.firestore.FirebaseFirestore db = FirebaseUtils.getFirestore();
        com.google.firebase.firestore.DocumentReference reviewRef = db.collection(Constants.COLLECTION_REVIEWS).document(reviewId);

        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot reviewSnap = transaction.get(reviewRef);
            if (reviewSnap.exists()) {
                String bookId = reviewSnap.getString("bookId");
                Double reviewRating = reviewSnap.getDouble("rating");
                
                if (bookId != null && reviewRating != null) {
                    com.google.firebase.firestore.DocumentReference bookRef = db.collection(Constants.COLLECTION_BOOKS).document(bookId);
                    com.google.firebase.firestore.DocumentSnapshot bookSnap = transaction.get(bookRef);
                    if (bookSnap.exists()) {
                        double currentRating = bookSnap.getDouble("rating") != null ? bookSnap.getDouble("rating") : 0;
                        long currentCount = bookSnap.getLong("ratingCount") != null ? bookSnap.getLong("ratingCount") : 0;
                        
                        if (currentCount > 1) {
                            double newRating = ((currentRating * currentCount) - reviewRating) / (currentCount - 1);
                            transaction.update(bookRef, "rating", newRating);
                            transaction.update(bookRef, "ratingCount", currentCount - 1);
                        } else {
                            transaction.update(bookRef, "rating", 0.0);
                            transaction.update(bookRef, "ratingCount", 0L);
                        }
                    }
                }
                transaction.delete(reviewRef);
            }
            return null;
        }).addOnSuccessListener(unused -> callback.onSuccess(null))
          .addOnFailureListener(callback::onFailure);
    }
}
