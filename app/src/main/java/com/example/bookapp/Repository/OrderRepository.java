package com.example.bookapp.Repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Order;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lớp DUY NHẤT được phép gọi Firestore cho collection "orders".
 * Dùng bởi CheckoutActivity (tạo đơn), OrderHistoryActivity (danh sách + lọc),
 * OrderDetailActivity (chi tiết + hủy đơn).
 */
public class OrderRepository {

    /** Tạo đơn hàng mới. Trả về orderId vừa tạo qua callback để Checkout điều hướng tiếp. */
    public void createOrder(Order order, FirebaseCallback<String> callback) {
        String orderId = order.getOrderId();
        if (orderId == null || orderId.isEmpty()) {
            orderId = UUID.randomUUID().toString();
            order.setOrderId(orderId);
        }

        final String finalOrderId = orderId;
        com.google.firebase.firestore.FirebaseFirestore db = FirebaseUtils.getFirestore();
        com.google.firebase.firestore.DocumentReference orderRef = db.collection(Constants.COLLECTION_ORDERS).document(finalOrderId);

        db.runTransaction(transaction -> {
            // Check stock for all items first
            for (com.example.bookapp.Model.OrderItem item : order.getItems()) {
                com.google.firebase.firestore.DocumentReference bookRef = db.collection(Constants.COLLECTION_BOOKS).document(item.getBookId());
                com.google.firebase.firestore.DocumentSnapshot bookSnap = transaction.get(bookRef);
                
                if (!bookSnap.exists()) {
                    throw new com.google.firebase.firestore.FirebaseFirestoreException(
                            "Sách không tồn tại: " + item.getTitle(),
                            com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
                }
                
                Long currentStock = bookSnap.getLong(Constants.FIELD_STOCK);
                if (currentStock == null || currentStock < item.getQuantity()) {
                    throw new com.google.firebase.firestore.FirebaseFirestoreException(
                            "Sách '" + item.getTitle() + "' không đủ số lượng tồn kho (còn " + (currentStock == null ? 0 : currentStock) + ")",
                            com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
                }
                
                // Reduce stock
                transaction.update(bookRef, Constants.FIELD_STOCK, currentStock - item.getQuantity());
            }
            
            // Create order
            transaction.set(orderRef, order);
            
            return finalOrderId;
        }).addOnSuccessListener(callback::onSuccess)
          .addOnFailureListener(callback::onFailure);
    }

    public LiveData<Order> getOrder(String orderId) {
        MutableLiveData<Order> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_ORDERS)
                .document(orderId)
                .get()
                .addOnSuccessListener(doc -> {
                    Order order = doc.toObject(Order.class);
                    if (order != null) order.setOrderId(doc.getId());
                    liveData.setValue(order);
                })
                .addOnFailureListener(e -> liveData.setValue(null));

        return liveData;
    }

    /**
     * Danh sách đơn của 1 user, lọc theo trạng thái nếu có (dùng cho Tab trong
     * OrderHistoryActivity). status = null hoặc "all" -> lấy toàn bộ đơn.
     */
    public LiveData<List<Order>> getOrdersByUser(String uid, @Nullable String status) {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();

        // Lấy tất cả đơn hàng của userId, bỏ qua .orderBy và .whereEqualTo("orderStatus")
        // để tránh lỗi FAILED_PRECONDITION do thiếu composite index trên Firestore.
        Query query = FirebaseUtils.getFirestore().collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo("userId", uid);

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Order> orders = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Order order = doc.toObject(Order.class);
                        order.setOrderId(doc.getId());
                        
                        // Lọc theo trạng thái trên máy khách (client-side)
                        boolean isAllTab = status == null || "all".equals(status);
                        if (isAllTab) {
                            orders.add(order);
                        } else if (status.equals(order.getOrderStatus())) {
                            orders.add(order);
                        }
                    });
                    
                    // Sắp xếp theo ngày tạo mới nhất (descending) trên máy khách
                    orders.sort((o1, o2) -> {
                        if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                        }
                        return 0;
                    });
                    
                    liveData.setValue(orders);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    public void updateOrderStatus(String orderId, String newStatus, FirebaseCallback<Void> callback) {
        com.google.firebase.firestore.FirebaseFirestore db = FirebaseUtils.getFirestore();
        db.collection(Constants.COLLECTION_ORDERS).document(orderId).get().addOnSuccessListener(doc -> {
            Order order = doc.toObject(Order.class);
            if (order == null) {
                callback.onFailure(new Exception("Order not found"));
                return;
            }
            
            String oldStatus = order.getOrderStatus();
            if (newStatus.equals(oldStatus)) {
                callback.onSuccess(null);
                return;
            }

            com.google.firebase.firestore.WriteBatch batch = db.batch();
            batch.update(db.collection(Constants.COLLECTION_ORDERS).document(orderId), "orderStatus", newStatus);
            
            com.google.firebase.firestore.CollectionReference booksRef = db.collection(Constants.COLLECTION_BOOKS);

            // If changing to CANCELLED from something else (Fix BUG-02)
            if (Constants.ORDER_CANCELLED.equals(newStatus)) {
                for (com.example.bookapp.Model.OrderItem item : order.getItems()) {
                    if (item.getBookId() != null) {
                        // Restore stock
                        batch.update(booksRef.document(item.getBookId()), 
                            Constants.FIELD_STOCK, com.google.firebase.firestore.FieldValue.increment(item.getQuantity()));
                        
                        // If it was delivered, we also need to revert the soldCount
                        if (Constants.ORDER_DELIVERED.equals(oldStatus)) {
                            batch.update(booksRef.document(item.getBookId()), 
                                Constants.FIELD_SOLD_COUNT, com.google.firebase.firestore.FieldValue.increment(-item.getQuantity()));
                        }
                    }
                }
            }

            batch.commit()
                    .addOnSuccessListener(unused -> callback.onSuccess(null))
                    .addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }

    public void deleteOrder(String orderId, FirebaseCallback<Void> callback) {
        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_ORDERS)
                .document(orderId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void checkUserPurchasedBook(String uid, String bookId, FirebaseCallback<Boolean> callback) {
        FirebaseUtils.getFirestore().collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo(Constants.FIELD_USER_ID, uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hasPurchased = false;
                    for (Order order : querySnapshot.toObjects(Order.class)) {
                        if (Constants.ORDER_DELIVERED.equals(order.getOrderStatus()) && order.getItems() != null) {
                            for (com.example.bookapp.Model.OrderItem item : order.getItems()) {
                                if (bookId.equals(item.getBookId())) {
                                    hasPurchased = true;
                                    break;
                                }
                            }
                        }
                        if (hasPurchased) break;
                    }
                    callback.onSuccess(hasPurchased);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
