package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Author;
import com.example.bookapp.R;
import com.example.bookapp.Utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_admin_simple_entity.xml — dùng cho ManageAuthorActivity. */
public class AdminAuthorAdapter extends RecyclerView.Adapter<AdminAuthorAdapter.AuthorViewHolder> {

    public interface OnAuthorActionListener {
        void onEditClick(Author author);

        void onDeleteClick(Author author);
    }

    private List<Author> authors = new ArrayList<>();
    private final OnAuthorActionListener listener;

    public AdminAuthorAdapter(OnAuthorActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Author> newItems) {
        this.authors = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AuthorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_admin_simple_entity, parent, false);
        return new AuthorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorViewHolder holder, int position) {
        Author author = authors.get(position);

        holder.tvName.setText(author.getName());
        holder.tvSubtitle.setText(author.getBookCount() + " đầu sách");
        ImageUtils.loadImage(holder.ivAvatar, author.getAvatarUrl(), R.drawable.ic_author);

        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(author);
        });
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(author);
        });
    }

    @Override
    public int getItemCount() {
        return authors.size();
    }

    static class AuthorViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivEdit, ivDelete;
        TextView tvName, tvSubtitle;

        AuthorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_entity_avatar);
            tvName = itemView.findViewById(R.id.tv_entity_name);
            tvSubtitle = itemView.findViewById(R.id.tv_entity_subtitle);
            ivEdit = itemView.findViewById(R.id.iv_entity_edit);
            ivDelete = itemView.findViewById(R.id.iv_entity_delete);
        }
    }
}
