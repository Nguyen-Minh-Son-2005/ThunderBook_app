package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Voucher;
import com.example.bookapp.Repository.AdminVoucherRepository;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.ArrayList;
import java.util.List;

/** Dùng cho ManageVoucherActivity. */
public class AdminVoucherViewModel extends ViewModel {

    public enum SortOption {EXPIRING_FIRST, NEWEST, VALUE_DESC}

    private final AdminVoucherRepository repository = new AdminVoucherRepository();

    private final LiveData<List<Voucher>> allVouchers = repository.observeAllVouchers();
    private final MediatorLiveData<List<Voucher>> displayedVouchers = new MediatorLiveData<>();
    private List<Voucher> cachedList = new ArrayList<>();

    private String currentKeyword = "";
    private SortOption currentSort = SortOption.EXPIRING_FIRST;

    private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminVoucherViewModel() {
        displayedVouchers.addSource(allVouchers, list -> {
            cachedList = list != null ? list : new ArrayList<>();
            applyFilterSort();
        });
    }

    public LiveData<List<Voucher>> getDisplayedVouchers() {
        return displayedVouchers;
    }

    public LiveData<Boolean> getActionSuccess() {
        return actionSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void search(String keyword) {
        currentKeyword = keyword;
        applyFilterSort();
    }

    public void setSortOption(SortOption option) {
        currentSort = option;
        applyFilterSort();
    }

    private void applyFilterSort() {
        List<Voucher> result = repository.filterByCode(cachedList, currentKeyword);
        switch (currentSort) {
            case VALUE_DESC:
                result = repository.sortByValueDesc(result);
                break;
            case EXPIRING_FIRST:
                result = repository.sortByExpiringFirst(result);
                break;
            case NEWEST:
            default:
                // Không có field createdAt trong Voucher theo schema gốc — giữ nguyên
                // thứ tự Firestore trả về (không orderBy cụ thể ở Repository).
                break;
        }
        displayedVouchers.setValue(result);
    }

    public void toggleActive(String voucherId, boolean isActive) {
        repository.toggleActive(voucherId, isActive, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                actionSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }

    public void deleteVoucher(String voucherId) {
        repository.deleteVoucher(voucherId, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                actionSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }
}
