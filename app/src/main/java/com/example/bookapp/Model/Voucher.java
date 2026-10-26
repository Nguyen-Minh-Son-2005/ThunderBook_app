package com.example.bookapp.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

/**
 * Model tương ứng bảng "vouchers".
 * Implement Parcelable để truyền đối tượng đầy đủ qua Intent (SelectVoucherActivity → CheckoutActivity).
 * Repository đọc/ghi thủ công bằng HashMap/DocumentSnapshot, KHÔNG dùng toObject().
 */
public class Voucher implements Parcelable {

    private String voucherId;
    private String code;
    private String kind;   // "percent" | "fixed" | "freeship"  (field Firestore: "kind")
    private double value;
    private Timestamp startDate;
    private Timestamp endDate;
    private boolean active; // field Firestore: "active"

    public Voucher() {
    }

    public Voucher(String voucherId, String code, String kind, double value,
                    Timestamp startDate, Timestamp endDate, boolean active) {
        this.voucherId = voucherId;
        this.code = code;
        this.kind = kind;
        this.value = value;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    // ===== Parcelable =====
    protected Voucher(Parcel in) {
        voucherId = in.readString();
        code = in.readString();
        kind = in.readString();
        value = in.readDouble();
        active = in.readByte() != 0;
        // Timestamp lưu dưới dạng seconds + nanos
        long startSeconds = in.readLong();
        int startNanos = in.readInt();
        if (startSeconds >= 0) startDate = new Timestamp(startSeconds, startNanos);
        long endSeconds = in.readLong();
        int endNanos = in.readInt();
        if (endSeconds >= 0) endDate = new Timestamp(endSeconds, endNanos);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(voucherId);
        dest.writeString(code);
        dest.writeString(kind);
        dest.writeDouble(value);
        dest.writeByte((byte) (active ? 1 : 0));
        if (startDate != null) {
            dest.writeLong(startDate.getSeconds());
            dest.writeInt(startDate.getNanoseconds());
        } else {
            dest.writeLong(-1L);
            dest.writeInt(0);
        }
        if (endDate != null) {
            dest.writeLong(endDate.getSeconds());
            dest.writeInt(endDate.getNanoseconds());
        } else {
            dest.writeLong(-1L);
            dest.writeInt(0);
        }
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Voucher> CREATOR = new Creator<Voucher>() {
        @Override
        public Voucher createFromParcel(Parcel in) { return new Voucher(in); }
        @Override
        public Voucher[] newArray(int size) { return new Voucher[size]; }
    };

    // ===== Getters & Setters =====
    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }

    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /**
     * Tính số tiền được giảm dựa trên tổng đơn hàng.
     */
    public double calculateDiscount(double orderTotal) {
        if (kind == null) return 0;
        switch (kind) {
            case "percent":
                return orderTotal * (value / 100.0);
            case "fixed":
                return Math.min(value, orderTotal);
            case "freeship":
                return 0; // phí ship = 0 được xử lý riêng trong CheckoutActivity
            default:
                return 0;
        }
    }
}
