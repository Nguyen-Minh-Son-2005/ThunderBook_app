package com.example.bookapp.Utils;

/**
 * Callback dùng chung cho các thao tác GHI (create/update/delete) trong mọi Repository.
 * Thao tác ĐỌC dùng LiveData thay vì callback này (xem từng Repository).
 *
 * Dùng generic <T> để tái sử dụng cho nhiều kiểu trả về khác nhau:
 * - FirebaseCallback<Void>   -> thao tác không cần trả dữ liệu (vd update, delete)
 * - FirebaseCallback<String> -> thao tác cần trả về id vừa tạo (vd createOrder trả orderId)
 */
public interface FirebaseCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);
}
