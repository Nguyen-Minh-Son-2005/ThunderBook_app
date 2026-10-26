package com.example.bookapp.Adapter.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Address;
import com.example.bookapp.R;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    public interface OnAddressActionListener {
        void onSelect(Address address);
        void onEdit(Address address);
        void onDelete(Address address);
    }

    private final List<Address> addresses;
    private final OnAddressActionListener listener;
    /** true nếu mở màn từ Checkout (chế độ chọn địa chỉ) -> hiện RadioButton */
    private final boolean selectMode;
    private String selectedAddressId;

    public AddressAdapter(List<Address> addresses, boolean selectMode, OnAddressActionListener listener) {
        this.addresses = addresses;
        this.selectMode = selectMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addresses.get(position);

        holder.tvNamePhone.setText(address.getName() + "  |  " + address.getPhone());
        holder.tvFullAddress.setText(address.getFullAddress());

        holder.rbSelect.setVisibility(selectMode ? View.VISIBLE : View.GONE);
        holder.rbSelect.setChecked(address.getAddressId().equals(selectedAddressId));

        holder.itemView.setOnClickListener(v -> {
            if (selectMode) {
                selectedAddressId = address.getAddressId();
                notifyDataSetChanged();
                listener.onSelect(address);
            }
        });

        holder.tvEdit.setOnClickListener(v -> listener.onEdit(address));
        holder.tvDelete.setOnClickListener(v -> listener.onDelete(address));
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public String getSelectedAddressId() {
        return selectedAddressId;
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbSelect;
        TextView tvNamePhone, tvFullAddress, tvEdit, tvDelete;

        AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            rbSelect = itemView.findViewById(R.id.rb_select);
            tvNamePhone = itemView.findViewById(R.id.tv_name_phone);
            tvFullAddress = itemView.findViewById(R.id.tv_full_address);
            tvEdit = itemView.findViewById(R.id.tv_edit);
            tvDelete = itemView.findViewById(R.id.tv_delete);
        }
    }
}
