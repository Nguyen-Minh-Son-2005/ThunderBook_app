package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.User;
import com.example.bookapp.Repository.AdminUserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Dùng cho ManageUserActivity — danh sách + lọc theo role + tìm kiếm.
 *
 * LƯU Ý: chỉ hỗ trợ sắp xếp "Tên A-Z" — Model User theo schema gốc KHÔNG có field
 * createdAt hay tổng chi tiêu, nên không thể làm "Mới nhất"/"Chi tiêu nhiều nhất" mà
 * không thêm field mới vào Model hoặc tính toán tốn kém (join từng user với orders).
 */
public class AdminUserViewModel extends ViewModel {

    public enum SortOption {DEFAULT, NAME_ASC}

    private final AdminUserRepository repository = new AdminUserRepository();

    private final LiveData<List<User>> allUsers = repository.observeAllUsers();
    private final MediatorLiveData<List<User>> displayedUsers = new MediatorLiveData<>();
    private List<User> cachedList = new ArrayList<>();

    private String currentKeyword = "";
    private String currentRole = null; // null = chip "Tất cả"
    private SortOption currentSort = SortOption.DEFAULT;

    public AdminUserViewModel() {
        displayedUsers.addSource(allUsers, list -> {
            cachedList = list != null ? list : new ArrayList<>();
            applyFilter();
        });
    }

    public LiveData<List<User>> getDisplayedUsers() {
        return displayedUsers;
    }

    public void search(String keyword) {
        currentKeyword = keyword;
        applyFilter();
    }

    /** role null = chip "Tất cả", dùng Constants.ROLE_ADMIN/ROLE_USER cho 2 chip còn lại. */
    public void setRoleFilter(String role) {
        currentRole = role;
        applyFilter();
    }

    public void setSortOption(SortOption option) {
        currentSort = option;
        applyFilter();
    }

    private void applyFilter() {
        List<User> result = repository.filterByNameOrEmail(cachedList, currentKeyword);
        result = repository.filterByRole(result, currentRole);
        if (currentSort == SortOption.NAME_ASC) {
            result = new ArrayList<>(result);
            result.sort((a, b) -> {
                String nameA = a.getFullName() != null ? a.getFullName() : "";
                String nameB = b.getFullName() != null ? b.getFullName() : "";
                return nameA.compareToIgnoreCase(nameB);
            });
        }
        displayedUsers.setValue(result);
    }
}
