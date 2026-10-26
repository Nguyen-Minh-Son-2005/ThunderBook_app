package com.example.bookapp.View.admin;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.ImageView;

/**
 * Dialog dùng CHUNG cho ManageCategoryActivity/ManageAuthorActivity/ManagePublisherActivity
 * — cả 3 màn có form giống hệt nhau (ảnh + tên + mô tả tùy chọn), chỉ khác nhãn hiển thị
 * và nơi dữ liệu được lưu vào (mỗi Activity tự quyết định gọi ViewModel nào trong
 * onSave()). Dialog CHỈ lo thu thập input, không tự gọi Repository/ViewModel — đúng
 * nguyên tắc tách UI khỏi logic nghiệp vụ.
 */
public class AdminEntityFormDialog extends DialogFragment {

    public interface OnSaveListener {
        /** existingId null nếu đang Thêm mới. newImageUri null nếu người dùng không đổi ảnh. */
        void onSave(@Nullable String existingId, String name, String description, @Nullable Uri newImageUri);
    }

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_SHOW_DESCRIPTION = "arg_show_description";
    private static final String ARG_DESCRIPTION_HINT = "arg_description_hint";
    private static final String ARG_EXISTING_ID = "arg_existing_id";
    private static final String ARG_EXISTING_NAME = "arg_existing_name";
    private static final String ARG_EXISTING_DESCRIPTION = "arg_existing_description";
    private static final String ARG_EXISTING_IMAGE_URL = "arg_existing_image_url";

    private OnSaveListener listener;
    private Uri pendingImageUri;
    private ImageView ivAvatar;

    public static AdminEntityFormDialog newInstanceAdd(String title, boolean showDescription, String descriptionHint) {
        AdminEntityFormDialog dialog = new AdminEntityFormDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putBoolean(ARG_SHOW_DESCRIPTION, showDescription);
        args.putString(ARG_DESCRIPTION_HINT, descriptionHint);
        dialog.setArguments(args);
        return dialog;
    }

    public static AdminEntityFormDialog newInstanceEdit(String title, boolean showDescription, String descriptionHint,
                                                          String existingId, String existingName,
                                                          @Nullable String existingDescription,
                                                          @Nullable String existingImageUrl) {
        AdminEntityFormDialog dialog = newInstanceAdd(title, showDescription, descriptionHint);
        dialog.getArguments().putString(ARG_EXISTING_ID, existingId);
        dialog.getArguments().putString(ARG_EXISTING_NAME, existingName);
        dialog.getArguments().putString(ARG_EXISTING_DESCRIPTION, existingDescription);
        dialog.getArguments().putString(ARG_EXISTING_IMAGE_URL, existingImageUrl);
        return dialog;
    }

    public void setOnSaveListener(OnSaveListener listener) {
        this.listener = listener;
    }

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                pendingImageUri = uri;
                ivAvatar.setImageURI(uri);
            });

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.admin_dialog_add_edit_simple_entity, null);

        android.widget.TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        ivAvatar = view.findViewById(R.id.iv_dialog_avatar);
        TextInputEditText etName = view.findViewById(R.id.et_dialog_name);
        TextInputLayout tilDescription = view.findViewById(R.id.til_dialog_description);
        TextInputEditText etDescription = view.findViewById(R.id.et_dialog_description);
        MaterialButton btnCancel = view.findViewById(R.id.btn_dialog_cancel);
        MaterialButton btnSave = view.findViewById(R.id.btn_dialog_save);

        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        boolean showDescription = args.getBoolean(ARG_SHOW_DESCRIPTION, false);
        String existingId = args.getString(ARG_EXISTING_ID);
        String existingName = args.getString(ARG_EXISTING_NAME);
        String existingDescription = args.getString(ARG_EXISTING_DESCRIPTION);
        String existingImageUrl = args.getString(ARG_EXISTING_IMAGE_URL);

        tvTitle.setText(existingId == null ? "Thêm " + args.getString(ARG_TITLE) : "Sửa " + args.getString(ARG_TITLE));
        tilDescription.setVisibility(showDescription ? View.VISIBLE : View.GONE);
        String descHint = args.getString(ARG_DESCRIPTION_HINT);
        if (descHint != null) tilDescription.setHint(descHint);

        if (existingName != null) etName.setText(existingName);
        if (existingDescription != null) etDescription.setText(existingDescription);
        if (existingImageUrl != null) ImageUtils.loadAvatar(ivAvatar, existingImageUrl);

        ivAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        view.findViewById(R.id.iv_dialog_avatar).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            if (name.isEmpty()) {
                etName.setError("Vui lòng nhập tên");
                return;
            }
            if (listener != null) listener.onSave(existingId, name, description, pendingImageUri);
            dismiss();
        });

        return new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }
}
