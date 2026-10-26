package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Address;
import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.CartItem;
import com.example.bookapp.Model.Order;
import com.example.bookapp.Model.OrderItem;
import com.example.bookapp.Model.Voucher;
import com.example.bookapp.Repository.AddressRepository;
import com.example.bookapp.Repository.CartRepository;
import com.example.bookapp.Repository.OrderRepository;
import com.example.bookapp.Repository.VoucherRepository;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ViewModel cho CheckoutActivity — file có nhiều Firestore call nhất project.
 * Tách 6 nguồn dữ liệu: cart, books, address, voucher, order, clearCart.
 *
 * Logic recalculateTotal() giữ trong Activity (thuần UI math, không gọi Firestore).
 */
public class CheckoutViewModel extends ViewModel {

    private final CartRepository cartRepository = new CartRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final VoucherRepository voucherRepository = new VoucherRepository();
    private final AddressRepository addressRepository = new AddressRepository();
    private final com.example.bookapp.Repository.BookRepository bookRepository =
            new com.example.bookapp.Repository.BookRepository();

    private final MutableLiveData<List<OrderItem>> _orderItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Address> _selectedAddress = new MutableLiveData<>();
    private final MutableLiveData<Voucher> _selectedVoucher = new MutableLiveData<>();
    private final MutableLiveData<String> _placeOrderResult = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<List<OrderItem>> getOrderItems() { return _orderItems; }
    public LiveData<Address> getSelectedAddress() { return _selectedAddress; }
    public LiveData<Voucher> getSelectedVoucher() { return _selectedVoucher; }
    /** Chứa orderId nếu đặt hàng thành công, null hoặc error message nếu thất bại. */
    public LiveData<String> getPlaceOrderResult() { return _placeOrderResult; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    /**
     * Đọc lại CartItem theo đúng danh sách bookId được chọn từ CartFragment,
     * join thêm thông tin sách để tạo snapshot OrderItem.
     */
    public void loadCheckoutItems(String uid, List<String> selectedBookIds) {
        if (uid == null || selectedBookIds.isEmpty()) return;

        List<OrderItem> items = new ArrayList<>();
        int[] remaining = {selectedBookIds.size()};

        FirebaseUtils.getFirestore().collection("carts").document(uid)
                .collection(Constants.SUBCOLLECTION_CART_ITEMS).get()
                .addOnSuccessListener(cartSnap -> {
                    List<CartItem> cartItems = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : cartSnap.getDocuments()) {
                        CartItem ci = doc.toObject(CartItem.class);
                        if (ci != null) {
                            ci.setBookId(doc.getId());
                            cartItems.add(ci);
                        }
                    }

                    for (String bookId : selectedBookIds) {
                        CartItem targetItem = null;
                        for (CartItem ci : cartItems) {
                            if (ci.getBookId().equals(bookId)) {
                                targetItem = ci;
                                break;
                            }
                        }

                        if (targetItem == null) {
                            remaining[0]--;
                            if (remaining[0] <= 0) _orderItems.setValue(items);
                            continue;
                        }

                        final CartItem finalCartItem = targetItem;
                        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_BOOKS)
                                .document(bookId).get()
                                .addOnSuccessListener(bookSnap -> {
                                    Book book = bookSnap.toObject(Book.class);
                                    if (book != null) {
                                        items.add(new OrderItem(
                                                bookId,
                                                book.getTitle(),
                                                book.getCoverImageUrl(),
                                                finalCartItem.getPriceAtAdd(),
                                                finalCartItem.getQuantity()
                                        ));
                                    }
                                    remaining[0]--;
                                    if (remaining[0] <= 0) _orderItems.setValue(items);
                                }).addOnFailureListener(e -> {
                                    remaining[0]--;
                                    if (remaining[0] <= 0) _orderItems.setValue(items);
                                });
                    }
                }).addOnFailureListener(e -> _errorMessage.setValue(e.getMessage()));
    }

    public void loadAddress(String uid, String addressId) {
        addressRepository.getAddress(uid, addressId).observeForever(address -> {
            if (address != null) _selectedAddress.setValue(address);
        });
    }

    public void loadDefaultAddress(String uid) {
        addressRepository.getAddresses(uid).observeForever(addresses -> {
            if (addresses != null && !addresses.isEmpty()) {
                _selectedAddress.setValue(addresses.get(0));
            } else {
                _selectedAddress.setValue(null);
            }
        });
    }

    public void loadVoucherByCode(String code) {
        voucherRepository.getVoucherByCode(code).observeForever(voucher ->
                _selectedVoucher.setValue(voucher));
    }

    /** Set voucher trực tiếp từ Parcelable (không cần query lại Firestore). */
    public void setSelectedVoucher(Voucher voucher) {
        _selectedVoucher.setValue(voucher);
    }

    public void placeOrder(String uid, List<OrderItem> orderItems, Address address,
                           Voucher voucher, double subtotal, double shippingFee,
                           double discount, String note, List<String> selectedBookIds) {
        String orderId = UUID.randomUUID().toString();

        double finalTotal = Math.max(subtotal + shippingFee - discount, 0);

        Order order = new Order(
                orderId, uid, orderItems, subtotal, shippingFee, discount,
                voucher != null ? voucher.getCode() : null,
                finalTotal, Constants.ORDER_PENDING, Constants.PAYMENT_PENDING,
                address, note, Timestamp.now()
        );

        orderRepository.createOrder(order, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String createdOrderId) {
                clearCart(uid, selectedBookIds);
                _placeOrderResult.setValue(createdOrderId);
            }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.setValue("Lỗi đặt hàng: " + e.getMessage());
            }
        });
    }

    public void clearCart(String uid, List<String> bookIds) {
        cartRepository.removeCartItems(uid, bookIds, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) { /* giỏ hàng đã được dọn */ }

            @Override
            public void onFailure(Exception e) { /* không nghiêm trọng, bỏ qua */ }
        });
    }
}
