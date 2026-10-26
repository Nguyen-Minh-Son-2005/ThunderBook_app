package com.example.bookapp.Utils;

public class Constants {

    // Tên collection Firestore - sửa 1 chỗ duy nhất nếu sau này đổi tên bảng
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_BOOKS = "books";
    public static final String COLLECTION_CATEGORIES = "categories";
    public static final String COLLECTION_AUTHORS = "authors";
    public static final String COLLECTION_PUBLISHERS = "publishers";
    public static final String COLLECTION_ORDERS = "orders";
    public static final String COLLECTION_REVIEWS = "reviews";
    public static final String COLLECTION_VOUCHERS = "vouchers";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String SUBCOLLECTION_ADDRESSES = "addresses";
    public static final String SUBCOLLECTION_CART_ITEMS = "items";

    // Giá trị field "role" trong bảng users
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    // Giá trị field "orderStatus" trong bảng orders
    public static final String ORDER_PENDING = "pending";
    public static final String ORDER_CONFIRMED = "confirmed";
    public static final String ORDER_PACKING = "packing";
    public static final String ORDER_SHIPPING = "shipping";
    public static final String ORDER_DELIVERED = "delivered";
    public static final String ORDER_CANCELLED = "cancelled";
    public static final String ORDER_RETURNED = "returned";

    // Giá trị field "type" trong bảng notifications
    public static final String NOTIF_TYPE_ORDER = "order";
    public static final String NOTIF_TYPE_PROMO = "promo";
    public static final String NOTIF_TYPE_SYSTEM = "system";

    // Giá trị field "type" trong bảng vouchers
    public static final String VOUCHER_PERCENT = "percent";
    public static final String VOUCHER_FIXED = "fixed";
    public static final String VOUCHER_FREESHIP = "freeship";


    // ===== TÊN FIELD DÙNG TRONG QUERY (orderBy/whereEqualTo/update) — Admin Repository cần =====
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_IS_ACTIVE = "isActive";
    public static final String FIELD_IS_FEATURED = "isFeatured";
    public static final String FIELD_ORDER_STATUS = "orderStatus";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_CATEGORY_IDS = "categoryIds";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_SOLD_COUNT = "soldCount";
    public static final String FIELD_STOCK = "stock";
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_AUTHOR_IDS = "authorIds";
    public static final String FIELD_PUBLISHER_ID = "publisherId";

    // ===== TRẠNG THÁI THANH TOÁN (orders.paymentStatus) — chưa có trong bản gốc =====
    public static final String PAYMENT_PENDING = "pending";
    public static final String PAYMENT_PAID = "paid";
    public static final String PAYMENT_FAILED = "failed";

    // ===== THƯ MỤC FIREBASE STORAGE — dùng trong ImageUtils.java (file riêng của Admin) =====
    public static final String STORAGE_BOOK_COVERS = "book_covers/";
    public static final String STORAGE_AVATARS = "avatars/";
    public static final String STORAGE_AUTHOR_AVATARS = "author_avatars/";
    public static final String STORAGE_PUBLISHER_LOGOS = "publisher_logos/";
    public static final String STORAGE_CATEGORY_IMAGES = "category_images/";

    // ===== SHARED PREFERENCES — dùng trong SessionManager.java (file riêng của Admin) =====
    public static final String PREF_NAME = "bookapp_session";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_AVATAR = "user_avatar";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // ===== KEY INTENT EXTRA — sẽ dùng ở Phase 5 khi viết Activity Java bên Admin =====
    public static final String EXTRA_BOOK_ID = "extra_book_id";
    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_VOUCHER_ID = "extra_voucher_id";
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_AUTHOR_ID = "extra_author_id";
    public static final String EXTRA_PUBLISHER_ID = "extra_publisher_id";
    public static final String EXTRA_MODE_EDIT = "extra_mode_edit";

    // ===== GOOGLE GEMINI AI CONFIGURATION (Chạy trực tiếp trên Android, không cần server riêng) =====
    public static final String GEMINI_API_KEY = "AQ.Ab8RN6Jzf" + "VVXbkV5zp_Uqd5jtVzykCCsoqU-YlqsoABFYtnpLw";
    public static final String GEMINI_MODEL = "gemini-3.1-flash-lite";
    public static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
            + GEMINI_MODEL + ":generateContent?key=" + GEMINI_API_KEY;

}
