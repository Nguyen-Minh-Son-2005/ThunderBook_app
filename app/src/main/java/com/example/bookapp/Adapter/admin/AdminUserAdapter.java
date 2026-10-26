package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.User;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_admin_user.xml — dùng cho ManageUserActivity. */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private List<User> users = new ArrayList<>();
    private final OnUserClickListener listener;

    public AdminUserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<User> newItems) {
        this.users = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        ImageUtils.loadImage(holder.ivAvatar, user.getAvatarUrl(), R.drawable.ic_user);

        boolean isAdmin = Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole());
        holder.tvRoleBadge.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        holder.tvRoleBadge.setText("Admin");

        // Bỏ qua hiển thị số đơn hàng trong danh sách rút gọn (quyết định đã chốt) —
        // ẩn hẳn View thay vì để text rỗng, tránh chừa khoảng trắng thừa trong item.
        // Muốn xem đầy đủ lịch sử mua hàng, vào AdminUserDetailActivity (đã có sẵn
        // AdminOrderRepository.getOrdersByUserId() phục vụ đúng màn đó).
        holder.tvOrderCount.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvEmail, tvRoleBadge, tvOrderCount;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvRoleBadge = itemView.findViewById(R.id.tv_user_role_badge);
            tvOrderCount = itemView.findViewById(R.id.tv_user_order_count);
        }
    }
}

