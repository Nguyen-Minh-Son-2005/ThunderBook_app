package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Voucher;
import com.example.bookapp.Repository.AdminVoucherRepository;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.Date;
import com.google.firebase.Timestamp;
/**
 * Dùng cho AddEditVoucherActivity.
 *
 * ⚠️ LƯU Ý: hàm saveVoucher() bên dưới gọi voucher.setStartDate(Date)/setEndDate(Date).
 * Rút kinh nghiệm từ lỗi bookCount vừa gặp — nếu Model Voucher khai 2 field này kiểu
 * com.google.firebase.Timestamp (giống getEndDate() bạn từng phải .toDate() để dùng),
 * thì setter cũng khả năng cao nhận Timestamp chứ không phải java.util.Date, dòng
 * gọi bên dưới sẽ báo lỗi "no candidates" y hệt bug bookCount. Nếu gặp lỗi đó, chỉ cần
 * đổi 2 dòng setStartDate/setEndDate thành:
 *   voucher.setStartDate(new com.google.firebase.Timestamp(startDate));
 *   voucher.setEndDate(new com.google.firebase.Timestamp(endDate));
 */
public class AdminAddEditVoucherViewModel extends ViewModel {

    private final AdminVoucherRepository repository = new AdminVoucherRepository();

    private final MutableLiveData<Voucher> loadedVoucher = new MutableLiveData<>();
    private final com.example.bookapp.Utils.SingleLiveEvent<Boolean> saveSuccess = new com.example.bookapp.Utils.SingleLiveEvent<>();
    private final com.example.bookapp.Utils.SingleLiveEvent<Boolean> deleteSuccess = new com.example.bookapp.Utils.SingleLiveEvent<>();
    private final com.example.bookapp.Utils.SingleLiveEvent<String> errorMessage = new com.example.bookapp.Utils.SingleLiveEvent<>();

    public LiveData<Voucher> getLoadedVoucher() {
        return loadedVoucher;
    }

    /** Gọi ở onCreate() nếu Intent có EXTRA_VOUCHER_ID (chế độ Sửa). */
    public void loadVoucherForEdit(String voucherId) {
        repository.getVoucherById(voucherId).observeForever(loadedVoucher::setValue);
    }

    /** Nút xóa trên iv_toolbar_action (chỉ hiện khi đang Sửa) — dùng chung repository đã có sẵn trong ViewModel này. */
    public void deleteVoucher(String voucherId) {
        repository.deleteVoucher(voucherId, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                deleteSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * voucherId null = Thêm mới, khác null = Sửa. type nhận Constants.VOUCHER_PERCENT/
     * VOUCHER_FIXED/VOUCHER_FREESHIP tương ứng 3 chip trong admin_activity_add_edit_voucher.xml.
     */
    public void saveVoucher(String voucherId, String code, String type, double value,
                             Date startDate, Date endDate, boolean isActive) {

        if (code == null || code.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập mã voucher");
            return;
        }
        if (startDate == null || endDate == null) {
            errorMessage.setValue("Vui lòng chọn ngày bắt đầu và kết thúc");
            return;
        }
        if (endDate.before(startDate)) {
            errorMessage.setValue("Ngày kết thúc phải sau ngày bắt đầu");
            return;
        }
        if (!Constants.VOUCHER_FREESHIP.equals(type) && value <= 0) {
            errorMessage.setValue("Giá trị giảm phải lớn hơn 0");
            return;
        }
        if (Constants.VOUCHER_PERCENT.equals(type) && value > 100) {
            errorMessage.setValue("Giảm theo % không được vượt quá 100");
            return;
        }

        Voucher voucher = new Voucher();
        voucher.setVoucherId(voucherId);
        voucher.setCode(code.trim().toUpperCase());
        voucher.setKind(type);
        voucher.setValue(value);
        voucher.setStartDate(startDate != null ? new Timestamp(startDate) : null);
        voucher.setEndDate(endDate != null ? new Timestamp(endDate) : null);
        voucher.setActive(isActive);

        FirebaseCallback<Void> callback = new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                saveSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        };

        if (voucherId == null) {
            repository.addVoucher(voucher, callback);
        } else {
            repository.updateVoucher(voucher, callback);
        }
    }
}
