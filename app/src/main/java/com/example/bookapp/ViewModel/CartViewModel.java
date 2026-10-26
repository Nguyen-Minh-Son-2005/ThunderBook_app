package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.CartItem;
import com.example.bookapp.Repository.BookRepository;
import com.example.bookapp.Repository.CartRepository;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel cho CartFragment.
 * Tách 4 lần gọi Firestore (loadCartItems, fetchBookInfo, updateQuantity, removeItem).
 */
public class CartViewModel extends ViewModel {

    private final CartRepository cartRepository = new CartRepository();
    private final BookRepository bookRepository = new BookRepository();

    private final MutableLiveData<List<CartItem>> _cartItems = new MutableLiveData<>();
    private final MutableLiveData<Map<String, String[]>> _bookInfoCache = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<String> _removeSuccessBookId = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<List<CartItem>> getCartItems() { return _cartItems; }
    public LiveData<Map<String, String[]>> getBookInfoCache() { return _bookInfoCache; }
    public LiveData<String> getRemoveSuccessBookId() { return _removeSuccessBookId; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    public void loadCart(String uid) {
        LiveData<List<CartItem>> liveData = cartRepository.getCartItems(uid);
        androidx.lifecycle.Observer<List<CartItem>> observer = new androidx.lifecycle.Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> items) {
                _cartItems.setValue(items);
                if (items != null) {
                    fetchBookInfo(items);
                }
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
    }

    /**
     * Join thêm title + coverUrl của từng sách trong giỏ.
     * Với số lượng giỏ hàng thường ít (< 20 item), get() riêng lẻ là đủ nhanh.
     */
    private void fetchBookInfo(List<CartItem> items) {
        Map<String, String[]> cache = _bookInfoCache.getValue();
        if (cache == null) cache = new HashMap<>();
        final Map<String, String[]> finalCache = cache;

        for (CartItem item : items) {
            if (finalCache.containsKey(item.getBookId())) continue;

            LiveData<Book> liveData = bookRepository.getBook(item.getBookId());
            androidx.lifecycle.Observer<Book> observer = new androidx.lifecycle.Observer<Book>() {
                @Override
                public void onChanged(Book book) {
                    if (book != null) {
                        finalCache.put(book.getBookId(),
                                new String[]{book.getTitle(), book.getCoverImageUrl()});
                        _bookInfoCache.setValue(finalCache);
                    }
                    liveData.removeObserver(this);
                }
            };
            liveData.observeForever(observer);
        }
    }

    public void updateQuantity(String uid, CartItem item, int newQuantity) {
        item.setQuantity(newQuantity);
        cartRepository.updateQuantity(uid, item.getBookId(), newQuantity, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) { /* cập nhật đã xảy ra locally trước rồi */ }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.setValue(e.getMessage());
            }
        });
    }

    public void removeItem(String uid, CartItem item) {
        cartRepository.removeCartItem(uid, item.getBookId(), new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<CartItem> current = _cartItems.getValue();
                if (current != null) {
                    current.remove(item);
                    _cartItems.setValue(current);
                }
                _removeSuccessBookId.setValue(item.getBookId());
            }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.setValue(e.getMessage());
            }
        });
    }
}
