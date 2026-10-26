package com.example.bookapp.Adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Voucher;
import com.example.bookapp.R;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.PriceFormatter;

import java.util.ArrayList;
import java.util.List;

/** Bind vào admin_item_admin_voucher.xml — dùng cho ManageVoucherActivity. */
public class AdminVoucherAdapter extends RecyclerView.Adapter<AdminVoucherAdapter.VoucherViewHolder> {

    public interface OnVoucherActionListener {
        void onItemClick(Voucher voucher);

        void onToggleActive(Voucher voucher, boolean newActiveState);
    }

    private List<Voucher> vouchers = new ArrayList<>();
    private final OnVoucherActionListener listener;

    public AdminVoucherAdapter(OnVoucherActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Voucher> newItems) {
        this.vouchers = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_admin_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = vouchers.get(position);

        holder.tvCode.setText(voucher.getCode());

        String valueLabel;
        String conditionLabel;
        if (Constants.VOUCHER_PERCENT.equals(voucher.getKind())) {
            valueLabel = ((int) voucher.getValue()) + "%";
            conditionLabel = "Giảm theo phần trăm đơn hàng";
        } else if (Constants.VOUCHER_FREESHIP.equals(voucher.getKind())) {
            valueLabel = "Ship";
            conditionLabel = "Miễn phí vận chuyển";
        } else {
            valueLabel = PriceFormatter.formatVND(voucher.getValue());
            conditionLabel = "Giảm trực tiếp trên đơn hàng";
        }
        holder.tvValue.setText(valueLabel);
        holder.tvCondition.setText(conditionLabel);
        holder.tvExpiry.setText("HSD: " + PriceFormatter.formatDate(voucher.getEndDate().toDate()));

        holder.swActive.setOnCheckedChangeListener(null);
        holder.swActive.setChecked(voucher.isActive());
        holder.swActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggleActive(voucher, isChecked);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(voucher);
        });
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvValue, tvCode, tvCondition, tvExpiry;
        SwitchCompat swActive;

        VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvValue = itemView.findViewById(R.id.tv_voucher_value);
            tvCode = itemView.findViewById(R.id.tv_voucher_code);
            tvCondition = itemView.findViewById(R.id.tv_voucher_condition);
            tvExpiry = itemView.findViewById(R.id.tv_voucher_expiry);
            swActive = itemView.findViewById(R.id.sw_voucher_active);
        }
    }
}
