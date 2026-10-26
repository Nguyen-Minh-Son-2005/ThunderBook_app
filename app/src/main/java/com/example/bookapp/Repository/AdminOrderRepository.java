package com.example.bookapp.Repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.Order;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import com.example.bookapp.Model.OrderItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderRepository {

    private final CollectionReference ordersRef =
            FirebaseUtils.getFirestore().collection(Constants.COLLECTION_ORDERS);

    /** Realtime toàn bộ đơn hàng, mới nhất trước — dùng cho ManageOrderActivity. */
    public LiveData<List<Order>> observeAllOrders() {
        Query query = ordersRef.orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING);
        return new FirestoreListLiveData<>(query, Order.class);
    }

    /** N đơn hàng gần nhất, lấy 1 lần — dùng cho khối "Đơn hàng gần đây" trên AdminDashboardActivity. */
    public LiveData<List<Order>> getRecentOrders(int limit) {
        MutableLiveData<List<Order>> result = new MutableLiveData<>();
        ordersRef.orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(snapshots -> result.setValue(snapshots.toObjects(Order.class)));
        return result;
    }

    /** 1 đơn hàng cụ thể, lấy 1 lần — dùng cho AdminOrderDetailActivity. */
    public LiveData<Order> getOrderById(String orderId) {
        MutableLiveData<Order> result = new MutableLiveData<>();
        ordersRef.document(orderId).get()
                .addOnSuccessListener(doc -> result.setValue(doc.toObject(Order.class)));
        return result;
    }

    /** Lịch sử mua hàng của 1 user cụ thể, lấy 1 lần — dùng cho màn chi tiết user phía admin. */
    public LiveData<List<Order>> getOrdersByUserId(String userId) {
        MutableLiveData<List<Order>> result = new MutableLiveData<>();
        ordersRef.whereEqualTo(Constants.FIELD_USER_ID, userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Order> orders = snapshots.toObjects(Order.class);
                    orders.sort((o1, o2) -> {
                        if (o1.getCreatedAt() == null && o2.getCreatedAt() == null) return 0;
                        if (o1.getCreatedAt() == null) return 1;
                        if (o2.getCreatedAt() == null) return -1;
                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                    });
                    result.setValue(orders);
                })
                .addOnFailureListener(e -> result.setValue(new ArrayList<>()));
        return result;
    }

    /**
     * Đổi trạng thái đơn hàng — dùng ở AdminOrderDetailActivity mỗi lần admin bấm nút
     * "Xác nhận đơn → Đang đóng gói" v.v. CHỈ cập nhật field orderStatus, KHÔNG tạo
     * notification ở đây — ViewModel gọi tiếp AdminNotificationRepository sau khi callback
     * này thành công, giữ mỗi Repository chỉ lo đúng 1 bảng.
     */
    public void updateOrderStatus(String orderId, String newStatus, FirebaseCallback<Void> callback) {
        ordersRef.document(orderId).get().addOnSuccessListener(doc -> {
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

            FirebaseFirestore db = FirebaseUtils.getFirestore();
            WriteBatch batch = db.batch();
            batch.update(ordersRef.document(orderId), Constants.FIELD_ORDER_STATUS, newStatus);
            
            CollectionReference booksRef = db.collection(Constants.COLLECTION_BOOKS);

            // If changing to DELIVERED from something else
            if (Constants.ORDER_DELIVERED.equals(newStatus)) {
                for (OrderItem item : order.getItems()) {
                    if (item.getBookId() != null) {
                        batch.update(booksRef.document(item.getBookId()), 
                            Constants.FIELD_SOLD_COUNT, FieldValue.increment(item.getQuantity()));
                    }
                }
            }
            // If changing to CANCELLED from something else (Fix BUG-02)
            else if (Constants.ORDER_CANCELLED.equals(newStatus)) {
                for (OrderItem item : order.getItems()) {
                    if (item.getBookId() != null) {
                        // Restore stock
                        batch.update(booksRef.document(item.getBookId()), 
                            Constants.FIELD_STOCK, FieldValue.increment(item.getQuantity()));
                        
                        // If it was delivered, we also need to revert the soldCount
                        if (Constants.ORDER_DELIVERED.equals(oldStatus)) {
                            batch.update(booksRef.document(item.getBookId()), 
                                Constants.FIELD_SOLD_COUNT, FieldValue.increment(-item.getQuantity()));
                        }
                    }
                }
            }

            batch.commit()
                    .addOnSuccessListener(unused -> callback.onSuccess(null))
                    .addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }

    /** Hủy đơn — tách riêng hàm cho rõ ý nghĩa nghiệp vụ dù dùng chung logic updateOrderStatus. */
    public void cancelOrder(String orderId, FirebaseCallback<Void> callback) {
        updateOrderStatus(orderId, Constants.ORDER_CANCELLED, callback);
    }

    /** Lọc theo trạng thái — dùng cho TabLayout (Tất cả/Chờ xử lý/Đóng gói/Đang giao/Đã giao/Đã hủy). */
    public List<Order> filterByStatus(List<Order> source, @Nullable String status) {
        if (status == null || status.isEmpty()) return source;
        List<Order> result = new ArrayList<>();
        for (Order o : source) {
            if (status.equalsIgnoreCase(o.getOrderStatus())) result.add(o);
        }
        return result;
    }

    /** Tìm theo mã đơn hoặc tên người nhận (lấy từ shippingAddress snapshot trong đơn). */
    public List<Order> filterByKeyword(List<Order> source, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return source;
        List<Order> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT).trim();
        for (Order o : source) {
            boolean matchId = o.getOrderId() != null
                    && o.getOrderId().toLowerCase(Locale.ROOT).contains(lowerKeyword);
            boolean matchName = o.getShippingAddress() != null
                    && o.getShippingAddress().getName() != null
                    && o.getShippingAddress().getName().toLowerCase(Locale.ROOT).contains(lowerKeyword);
            if (matchId || matchName) result.add(o);
        }
        return result;
    }

    /**
     * Thống kê doanh thu trong khoảng thời gian, lấy 1 lần — Firestore không hỗ trợ SUM ở
     * tầng server (trừ khi dùng Cloud Functions trả phí), nên cộng dồn ở client. Chỉ tính
     * đơn có orderStatus = delivered.
     */
    public LiveData<Double> getRevenueInRange(Date fromDate, Date toDate) {
        MutableLiveData<Double> result = new MutableLiveData<>();
        ordersRef.whereGreaterThanOrEqualTo(Constants.FIELD_CREATED_AT, fromDate)
                .whereLessThanOrEqualTo(Constants.FIELD_CREATED_AT, toDate)
                .get()
                .addOnSuccessListener(snapshots -> {
                    double total = 0;
                    for (Order order : snapshots.toObjects(Order.class)) {
                        if (Constants.ORDER_DELIVERED.equals(order.getOrderStatus())) {
                            total += order.getFinalTotal();
                        }
                    }
                    result.setValue(total);
                })
                .addOnFailureListener(e -> result.setValue(0.0));
        return result;
    }

    /**
     * Trả về NGUYÊN danh sách đơn đã giao trong khoảng thời gian (không chỉ tổng tiền) —
     * dùng cho StatisticActivity cần tính CẢ doanh thu, số đơn, giá trị trung bình/đơn
     * từ CÙNG 1 lần query, tránh gọi Firestore 3 lần cho 3 con số liên quan tới nhau.
     */
    public LiveData<List<Order>> getOrdersInRange(Date fromDate, Date toDate) {
        MutableLiveData<List<Order>> result = new MutableLiveData<>();
        ordersRef.whereGreaterThanOrEqualTo(Constants.FIELD_CREATED_AT, fromDate)
                .whereLessThanOrEqualTo(Constants.FIELD_CREATED_AT, toDate)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Order> deliveredOrders = new ArrayList<>();
                    for (Order order : snapshots.toObjects(Order.class)) {
                        if (Constants.ORDER_DELIVERED.equals(order.getOrderStatus())) {
                            deliveredOrders.add(order);
                        }
                    }
                    result.setValue(deliveredOrders);
                })
                .addOnFailureListener(e -> result.setValue(new ArrayList<>()));
        return result;
    }

    /** Đếm tổng số đơn — dùng cho thẻ thống kê "Đơn hàng" trên Dashboard. */
    public LiveData<Integer> countAllOrders() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        ordersRef.get().addOnSuccessListener(snapshots -> result.setValue(snapshots.size()));
        return result;
    }
}
