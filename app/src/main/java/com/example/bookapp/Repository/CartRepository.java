package com.example.bookapp.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookapp.Model.CartItem;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp DUY NHẤT được phép gọi Firestore cho subcollection "carts/{uid}/items".
 * Dùng bởi BookDetailActivity (thêm giỏ), CartFragment, CheckoutActivity.
 */
public class CartRepository {

    private static final String COLLECTION_CARTS = "carts";

    public LiveData<List<CartItem>> getCartItems(String uid) {
        MutableLiveData<List<CartItem>> liveData = new MutableLiveData<>();

        FirebaseUtils.getFirestore()
                .collection(COLLECTION_CARTS).document(uid)
                .collection(Constants.SUBCOLLECTION_CART_ITEMS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CartItem> items = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        CartItem item = doc.toObject(CartItem.class);
                        item.setBookId(doc.getId());
                        items.add(item);
                    });
                    liveData.setValue(items);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }

    public void addToCart(String uid, String bookId, int quantity, double priceAtAdd,
                           FirebaseCallback<Void> callback) {
        com.google.firebase.firestore.DocumentReference docRef = FirebaseUtils.getFirestore()
                .collection(COLLECTION_CARTS).document(uid)
                .collection(Constants.SUBCOLLECTION_CART_ITEMS).document(bookId);

        FirebaseUtils.getFirestore().runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(docRef);
            if (snapshot.exists()) {
                Long currentQuantity = snapshot.getLong("quantity");
                int newQuantity = (currentQuantity != null ? currentQuantity.intValue() : 0) + quantity;
                transaction.update(docRef, "quantity", newQuantity);
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("bookId", bookId);
                data.put("quantity", quantity);
                data.put("priceAtAdd", priceAtAdd);
                data.put("addedAt", Timestamp.now());
                transaction.set(docRef, data);
            }
            return null;
        }).addOnSuccessListener(unused -> callback.onSuccess(null))
          .addOnFailureListener(callback::onFailure);
    }

    public void updateQuantity(String uid, String bookId, int newQuantity, FirebaseCallback<Void> callback) {
        FirebaseUtils.getFirestore()
                .collection(COLLECTION_CARTS).document(uid)
                .collection(Constants.SUBCOLLECTION_CART_ITEMS).document(bookId)
                .update("quantity", newQuantity)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void removeCartItem(String uid, String bookId, FirebaseCallback<Void> callback) {
        FirebaseUtils.getFirestore()
                .collection(COLLECTION_CARTS).document(uid)
                .collection(Constants.SUBCOLLECTION_CART_ITEMS).document(bookId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /** Xóa nhiều item cùng lúc sau khi đặt hàng thành công (CheckoutActivity). */
    public void removeCartItems(String uid, List<String> bookIds, FirebaseCallback<Void> callback) {
        com.google.firebase.firestore.WriteBatch batch = FirebaseUtils.getFirestore().batch();

        for (String bookId : bookIds) {
            batch.delete(FirebaseUtils.getFirestore()
                    .collection(COLLECTION_CARTS).document(uid)
                    .collection(Constants.SUBCOLLECTION_CART_ITEMS).document(bookId));
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}
