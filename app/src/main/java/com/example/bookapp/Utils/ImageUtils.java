package com.example.bookapp.Utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.bookapp.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Upload ảnh lên SUPABASE STORAGE (KHÔNG PHẢI Firebase Storage) — nhóm dùng Supabase để
 * lưu file ảnh, Firestore chỉ lưu lại downloadUrl trả về từ đây. Vì Supabase không có SDK
 * Java chính thức cho Android (chỉ có supabase-kt cho Kotlin), gọi thẳng REST API của
 * Supabase Storage bằng OkHttp.
 *
 * ⚠️ TODO TRƯỚC KHI NỘP BÀI: 2 hằng số SUPABASE_URL/SUPABASE_ANON_KEY đang hard-code thẳng
 * trong code cho nhanh chạy thử. Nếu repo Git là public, nên chuyển sang đọc từ
 * local.properties → BuildConfig field, tránh lộ key khi đẩy code lên GitHub (dù đây là
 * anon/publishable key, ít nhạy cảm hơn service_role key, nhưng vẫn nên tách cấu hình ra
 * khỏi source code theo best practice).
 */
public class ImageUtils {

    private static final String SUPABASE_URL = "https://lgwiftzrrebphjymvbbc.supabase.co";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_5UsboCEFR-YbFphQOdyjIg_TBYID2dO";
    private static final String BUCKET = "DL_SACH";

    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ExecutorService uploadExecutor = Executors.newSingleThreadExecutor();

    public interface OnUploadCompleteListener {
        void onSuccess(String downloadUrl);

        void onFailure(Exception e);
    }

    private ImageUtils() {
    }

    /**
     * Upload 1 ảnh lên Supabase Storage, trả về public URL để lưu vào Firestore.
     * Chạy NGẦM trên background thread (network không được chạy trên Main thread), callback
     * luôn được gọi lại trên Main thread để Activity cập nhật UI (Toast, ẩn loading...) an toàn.
     *
     * @param context  cần Context để đọc byte[] từ Uri qua ContentResolver — truyền
     *                 activity/fragment context lúc gọi (vd AddEditBookActivity.this)
     * @param folder   thư mục con trong bucket DL_SACH — dùng hằng số Constants.STORAGE_*
     */
    public static void uploadImage(@NonNull Context context, @NonNull Uri imageUri,
                                    @NonNull String folder, @NonNull OnUploadCompleteListener listener) {
        Context appContext = context.getApplicationContext();

        uploadExecutor.execute(() -> {
            try {
                byte[] bytes = readBytes(appContext, imageUri);
                String mimeType = getMimeType(appContext, imageUri);
                String extension = mimeType.contains("png") ? ".png" : ".jpg";
                String path = folder + UUID.randomUUID() + extension;

                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + path;

                RequestBody body = RequestBody.create(bytes, MediaType.parse(mimeType));
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                        .addHeader("Content-Type", mimeType)
                        .post(body)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorDetail = response.body() != null ? response.body().string() : "";
                        postFailure(listener, new IOException(
                                "Upload thất bại (mã " + response.code() + "): " + errorDetail));
                        return;
                    }
                }

                String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + path;
                postSuccess(listener, publicUrl);

            } catch (Exception e) {
                postFailure(listener, e);
            }
        });
    }

    private static byte[] readBytes(Context context, Uri uri) throws IOException {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is == null) throw new IOException("Không đọc được ảnh đã chọn");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }

    private static String getMimeType(Context context, Uri uri) {
        String type = context.getContentResolver().getType(uri);
        return type != null ? type : "image/jpeg";
    }

    private static void postSuccess(OnUploadCompleteListener listener, String url) {
        new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess(url));
    }

    private static void postFailure(OnUploadCompleteListener listener, Exception e) {
        new Handler(Looper.getMainLooper()).post(() -> listener.onFailure(e));
    }

    // ===== PHẦN TẢI ẢNH HIỂN THỊ — không đổi, vẫn dùng Glide load thẳng từ URL bất kỳ
    // (Supabase hay Firebase URL đều là URL công khai bình thường, Glide không quan tâm
    // nguồn gốc, chỉ cần đúng URL trỏ tới ảnh) =====

    public static void loadImage(@NonNull ImageView imageView, @Nullable String imageUrl) {
        loadImage(imageView, imageUrl, R.drawable.bg_thumbnail_placeholder);
    }

    public static void loadImage(@NonNull ImageView imageView, @Nullable String imageUrl,
                                  @DrawableRes int placeholderResId) {
        Glide.with(imageView.getContext())
                .load(imageUrl)
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    public static void loadAvatar(@NonNull ImageView imageView, @Nullable String avatarUrl) {
        loadImage(imageView, avatarUrl, R.drawable.bg_thumbnail_placeholder);
    }
}
