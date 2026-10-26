package com.example.bookapp.Repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Book;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DUY NHẤT được phép gọi Firestore cho collection "books".
 * Dùng bởi HomeFragment, SearchActivity, BookDetailActivity, CartFragment,
 * CheckoutActivity, MyReviewsActivity, WriteReviewActivity.
 */
public class BookRepository {

    public LiveData<Book> getBook(String bookId) {
        MutableLiveData<Book> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_BOOKS)
                .document(bookId)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(doc -> {
                    Book book = doc.toObject(Book.class);
                    if (book != null) book.setBookId(doc.getId());
                    liveData.setValue(book);
                })
                .addOnFailureListener(e -> {
                    // Fallback to cache if server unavailable
                    FirebaseUtils.getFirestore().collection(Constants.COLLECTION_BOOKS)
                            .document(bookId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                Book book = doc.toObject(Book.class);
                                if (book != null) book.setBookId(doc.getId());
                                liveData.setValue(book);
                            })
                            .addOnFailureListener(e2 -> liveData.setValue(null));
                });

        return liveData;
    }

    /** Sách ghim isFeatured=true - dùng cho khối "Nổi bật hôm nay" ở HomeFragment. */
    public LiveData<List<Book>> getFeaturedBooks() {
        MutableLiveData<List<Book>> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_BOOKS)
                .whereEqualTo(Constants.FIELD_IS_FEATURED, true)
                .whereEqualTo(Constants.FIELD_IS_ACTIVE, true)
                .get()
                .addOnSuccessListener(qs -> liveData.setValue(toBookList(qs)))
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    /** Toàn bộ sách đang bán - dùng cho khối "Dành cho bạn" ở HomeFragment. */
    public LiveData<List<Book>> getAllActiveBooks() {
        MutableLiveData<List<Book>> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_BOOKS)
                .whereEqualTo(Constants.FIELD_IS_ACTIVE, true)
                .get()
                .addOnSuccessListener(qs -> liveData.setValue(toBookList(qs)))
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    public LiveData<List<Book>> getRelatedBooks(String excludeBookId, String categoryId) {
        MutableLiveData<List<Book>> liveData = new MutableLiveData<>();

        Query query = FirebaseUtils.getFirestore().collection(Constants.COLLECTION_BOOKS)
                .whereEqualTo(Constants.FIELD_IS_ACTIVE, true);
        if (categoryId != null && !categoryId.isEmpty()) {
            query = query.whereArrayContains(Constants.FIELD_CATEGORY_IDS, categoryId);
        }
        
        query.limit(10)
                .get()
                .addOnSuccessListener(qs -> {
                    List<Book> books = new ArrayList<>();
                    qs.forEach(doc -> {
                        if (!doc.getId().equals(excludeBookId)) {
                            Book book = doc.toObject(Book.class);
                            book.setBookId(doc.getId());
                            books.add(book);
                        }
                    });
                    liveData.setValue(books);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    /**
     * Tìm kiếm sách theo tên/thể loại/sắp xếp - logic chuyển nguyên từ SearchActivity.
     * Firestore không hỗ trợ full-text search: tìm theo tiền tố tên sách
     * (title >= keyword && title <= keyword + "\uf8ff"). Nếu cần tìm gần đúng/toàn văn
     * thật sự, nên tích hợp Algolia hoặc Typesense ở bản nâng cấp sau.
     */
    public LiveData<List<Book>> searchBooks(String keyword, @Nullable String categoryId, String sortKey) {
        MutableLiveData<List<Book>> liveData = new MutableLiveData<>();

        Query query = FirebaseUtils.getFirestore().collection(Constants.COLLECTION_BOOKS)
                .whereEqualTo(Constants.FIELD_IS_ACTIVE, true);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String q = keyword.trim();
            // \uf8ff is a very high unicode code point used to simulate prefix search
            query = query.whereGreaterThanOrEqualTo("title", q)
                         .whereLessThanOrEqualTo("title", q + "\uf8ff");
        }

        if (categoryId != null) {
            query = query.whereArrayContains("categoryIds", categoryId);
        }

        // Bỏ toàn bộ việc sắp xếp trên Firestore để tránh lỗi Composite Index
        // Ta sẽ tự sort bằng Java dưới app sau khi lấy data về.

        query.limit(100).get()
                .addOnSuccessListener(qs -> {
                    List<Book> books = toBookList(qs);
                    
                    // Sort thủ công bằng Java
                    switch (sortKey) {
                        case "price_asc":
                            java.util.Collections.sort(books, (b1, b2) -> Double.compare(b1.getPrice(), b2.getPrice()));
                            break;
                        case "price_desc":
                            java.util.Collections.sort(books, (b1, b2) -> Double.compare(b2.getPrice(), b1.getPrice()));
                            break;
                        case "rating":
                            java.util.Collections.sort(books, (b1, b2) -> Double.compare(b2.getRating(), b1.getRating()));
                            break;
                        case "bestseller":
                        default:
                            java.util.Collections.sort(books, (b1, b2) -> Integer.compare(b2.getSoldCount(), b1.getSoldCount()));
                            break;
                    }
                    
                    liveData.setValue(books);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("BookRepository", "Lỗi searchBooks: ", e);
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    /** Tăng viewCount mỗi lần xem chi tiết sách - không cần chờ kết quả trả về. */
    public void incrementViewCount(String bookId) {
        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_BOOKS)
                .document(bookId)
                .update("viewCount", com.google.firebase.firestore.FieldValue.increment(1));
    }

    private List<Book> toBookList(com.google.firebase.firestore.QuerySnapshot querySnapshot) {
        List<Book> books = new ArrayList<>();
        querySnapshot.forEach(doc -> {
            Book book = doc.toObject(Book.class);
            book.setBookId(doc.getId());
            books.add(book);
        });
        return books;
    }
}
