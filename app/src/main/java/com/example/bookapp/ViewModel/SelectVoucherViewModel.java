package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Voucher;
import com.example.bookapp.Repository.VoucherRepository;

import java.util.List;

/**
 * ViewModel cho SelectVoucherActivity.
 * Tách load voucher + kiểm tra mã nhập tay ra khỏi Activity.
 */
public class SelectVoucherViewModel extends ViewModel {

    private final VoucherRepository voucherRepository = new VoucherRepository();

    private final MutableLiveData<List<Voucher>> _vouchers = new MutableLiveData<>();
    public LiveData<List<Voucher>> getVouchers() { return _vouchers; }

    public void loadAvailableVouchers() {
        voucherRepository.getAvailableVouchers().observeForever(
                vouchers -> _vouchers.setValue(vouchers));
    }

    public interface ValidationCallback {
        void onResult(boolean isValid);
    }

    /** Kiểm tra mã voucher nhập tay có hợp lệ không. */
    public void validateCode(String code, ValidationCallback callback) {
        LiveData<Voucher> liveData = voucherRepository.getVoucherByCode(code);
        liveData.observeForever(new androidx.lifecycle.Observer<Voucher>() {
            @Override
            public void onChanged(Voucher voucher) {
                callback.onResult(voucher != null);
                liveData.removeObserver(this);
            }
        });
    }
}
