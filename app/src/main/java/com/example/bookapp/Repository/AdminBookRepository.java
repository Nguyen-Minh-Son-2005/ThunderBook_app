package com.example.bookapp.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Book;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminBookRepository {

    private final CollectionReference booksRef =
            FirebaseUtils.getFirestore().collection(Constants.COLLECTION_BOOKS);

    /**
     * Realtime toàn bộ sách, mới nhất trước — dùng cho ManageBookActivity. Trả LiveData
     * tự gắn/gỡ listener theo vòng đời (xem FirestoreListLiveData) — ViewModel không cần
     * tự quản lý ListenerRegistration nữa.
     */
    public LiveData<List<Book>> observeAllBooks() {
        return new FirestoreListLiveData<>(booksRef, Book.class);
    }

    /** Lấy 1 lần (không realtime) — dùng khi mở AddEditBookActivity ở chế độ Sửa. */
    public LiveData<Book> getBookById(String bookId) {
        MutableLiveData<Book> result = new MutableLiveData<>();
        booksRef.document(bookId).get()
                .addOnSuccessListener(doc -> result.setValue(doc.toObject(Book.class)));
        return result;
    }

    /**
     * Thêm sách mới — tự sinh id bằng Firestore rồi gán ngược lại vào bookId để đồng bộ
     * giữa docId và field bookId trong document.
     */
    public void addBook(@NonNull Book book, FirebaseCallback<Void> callback) {
        DocumentReference docRef = booksRef.document();
        book.setBookId(docRef.getId());
        book.setSoldCount(0);
        book.setViewCount(0);
        book.setRating(0);
        book.setRatingCount(0);
        docRef.set(book)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateBook(@NonNull Book book, FirebaseCallback<Void> callback) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("title", book.getTitle());
        updates.put("description", book.getDescription());
        updates.put("coverImageUrl", book.getCoverImageUrl());
        updates.put("authorIds", book.getAuthorIds());
        updates.put("publisherId", book.getPublisherId());
        updates.put("categoryIds", book.getCategoryIds());
        updates.put("price", book.getPrice());
        updates.put("salePrice", book.getSalePrice());
        updates.put("stock", book.getStock());
        updates.put("isFeatured", book.isFeatured());
        updates.put("isActive", book.isActive());

        booksRef.document(book.getBookId()).update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /** Ẩn/hiện sách nhanh bằng switch trên item_admin_book — chỉ update 1 field. */
    public void toggleActive(String bookId, boolean isActive, FirebaseCallback<Void> callback) {
        booksRef.document(bookId).update(Constants.FIELD_IS_ACTIVE, isActive)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * "Xóa" sách = xóa mềm (isActive = false), KHÔNG xóa document thật — vì các đơn hàng cũ
     * đã snapshot thông tin sách trong orders.items, xóa cứng làm mất khả năng admin xem
     * lại sách gốc dù không ảnh hưởng dữ liệu đơn hàng.
     */
    public void softDeleteBook(String bookId, FirebaseCallback<Void> callback) {
        toggleActive(bookId, false, callback);
    }

    /**
     * Tìm theo tên sách — Firestore không hỗ trợ LIKE %keyword%, nên lọc client-side trên
     * danh sách đã có từ observeAllBooks(). Không query lại Firestore mỗi lần gõ phím.
     */
    public List<Book> filterByTitle(List<Book> source, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return source;
        List<Book> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT).trim();
        for (Book book : source) {
            if (book.getTitle() != null && book.getTitle().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
                result.add(book);
            }
        }
        return result;
    }

    /** Top sách bán chạy — lấy 1 lần, dùng cho AdminDashboardActivity và StatisticActivity. */
    public LiveData<List<Book>> getTopSellingBooks(int limit) {
        MutableLiveData<List<Book>> result = new MutableLiveData<>();
        booksRef.whereGreaterThan(Constants.FIELD_SOLD_COUNT, 0)
                .orderBy(Constants.FIELD_SOLD_COUNT, Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(snapshots -> result.setValue(snapshots.toObjects(Book.class)));
        return result;
    }

    /** Đếm nhanh tổng số sách đang mở bán — dùng cho thẻ thống kê "Sản phẩm" trên Dashboard. */
    public LiveData<Integer> countActiveBooks() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        booksRef.whereEqualTo(Constants.FIELD_IS_ACTIVE, true)
                .get()
                .addOnSuccessListener(snapshots -> result.setValue(snapshots.size()));
        return result;
    }

    /** Kiểm tra xem Thể loại có đang được dùng bởi sách nào không (hỗ trợ xóa an toàn). */
    public void checkCategoryInUse(String categoryId, FirebaseCallback<Boolean> callback) {
        booksRef.whereArrayContains(Constants.FIELD_CATEGORY_IDS, categoryId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> callback.onSuccess(!snapshots.isEmpty()))
                .addOnFailureListener(callback::onFailure);
    }

    /** Kiểm tra xem Tác giả có đang được dùng bởi sách nào không (hỗ trợ xóa an toàn). */
    public void checkAuthorInUse(String authorId, FirebaseCallback<Boolean> callback) {
        booksRef.whereArrayContains(Constants.FIELD_AUTHOR_IDS, authorId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> callback.onSuccess(!snapshots.isEmpty()))
                .addOnFailureListener(callback::onFailure);
    }

    /** Kiểm tra xem NXB có đang được dùng bởi sách nào không (hỗ trợ xóa an toàn). */
    public void checkPublisherInUse(String publisherId, FirebaseCallback<Boolean> callback) {
        booksRef.whereEqualTo(Constants.FIELD_PUBLISHER_ID, publisherId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> callback.onSuccess(!snapshots.isEmpty()))
                .addOnFailureListener(callback::onFailure);
    }
}
