package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Author;
import com.example.bookapp.Model.Book;
import com.example.bookapp.Repository.AdminAuthorRepository;
import com.example.bookapp.Repository.AdminBookRepository;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Dùng cho ManageBookActivity. */
public class AdminBookViewModel extends ViewModel {

    public enum SortOption {NEWEST, PRICE_ASC, PRICE_DESC, BEST_SELLING}

    public enum StockFilter {ALL, IN_STOCK, OUT_OF_STOCK, HIDDEN}

    private final AdminBookRepository repository = new AdminBookRepository();
    private final AdminAuthorRepository authorRepository = new AdminAuthorRepository();

    private final LiveData<List<Book>> allBooks = repository.observeAllBooks();
    private final LiveData<List<Author>> allAuthors = authorRepository.observeAllAuthors();
    
    private final MediatorLiveData<List<Book>> displayedBooks = new MediatorLiveData<>();
    private List<Book> cachedBookList = new ArrayList<>();
    private List<Author> cachedAuthorList = new ArrayList<>();

    private String currentKeyword = "";
    private SortOption currentSort = SortOption.NEWEST;
    private StockFilter currentStockFilter = StockFilter.ALL;
    private String currentCategoryId = null; // null = không lọc theo thể loại
    private String currentAuthorId = null; // null = không lọc theo tác giả
    private String currentPublisherId = null; // null = không lọc theo nxb

    private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminBookViewModel() {
        displayedBooks.addSource(allBooks, list -> {
            cachedBookList = list != null ? list : new ArrayList<>();
            applyFilterSort();
        });
        displayedBooks.addSource(allAuthors, list -> {
            cachedAuthorList = list != null ? list : new ArrayList<>();
            applyFilterSort();
        });
    }

    public LiveData<List<Book>> getDisplayedBooks() {
        return displayedBooks;
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

    /** Ứng với 4 chip chip_all/chip_in_stock/chip_out_stock/chip_hidden trong admin_activity_manage_book.xml. */
    public void setStockFilter(StockFilter filter) {
        currentStockFilter = filter;
        applyFilterSort();
    }

    /** null = bỏ lọc thể loại — dùng khi bấm chip_category_filter rồi chọn 1 thể loại cụ thể. */
    public void setCategoryFilter(String categoryId) {
        currentCategoryId = categoryId;
        applyFilterSort();
    }

    public void setAuthorFilter(String authorId) {
        currentAuthorId = authorId;
        applyFilterSort();
    }

    public void setPublisherFilter(String publisherId) {
        currentPublisherId = publisherId;
        applyFilterSort();
    }

    private void applyFilterSort() {
        List<Book> result = repository.filterByTitle(cachedBookList, currentKeyword);

        if (currentCategoryId != null) {
            List<Book> byCategory = new ArrayList<>();
            for (Book b : result) {
                if (b.getCategoryIds() != null && b.getCategoryIds().contains(currentCategoryId)) {
                    byCategory.add(b);
                }
            }
            result = byCategory;
        }

        if (currentAuthorId != null) {
            List<Book> byAuthor = new ArrayList<>();
            for (Book b : result) {
                if (b.getAuthorIds() != null && b.getAuthorIds().contains(currentAuthorId)) {
                    byAuthor.add(b);
                }
            }
            result = byAuthor;
        }

        if (currentPublisherId != null) {
            List<Book> byPublisher = new ArrayList<>();
            for (Book b : result) {
                if (currentPublisherId.equals(b.getPublisherId())) {
                    byPublisher.add(b);
                }
            }
            result = byPublisher;
        }

        switch (currentStockFilter) {
            case IN_STOCK:
                result = filterBy(result, b -> b.getStock() > 0 && b.isActive());
                break;
            case OUT_OF_STOCK:
                result = filterBy(result, b -> b.getStock() <= 0);
                break;
            case HIDDEN:
                result = filterBy(result, b -> !b.isActive());
                break;
            case ALL:
            default:
                // không lọc gì thêm
                break;
        }

        List<Book> sorted = new ArrayList<>(result);
        switch (currentSort) {
            case PRICE_ASC:
                sorted.sort(Comparator.comparingDouble(b -> b.getSalePrice() > 0 ? b.getSalePrice() : b.getPrice()));
                break;
            case PRICE_DESC:
                sorted.sort((a, b) -> {
                    double priceA = a.getSalePrice() > 0 ? a.getSalePrice() : a.getPrice();
                    double priceB = b.getSalePrice() > 0 ? b.getSalePrice() : b.getPrice();
                    return Double.compare(priceB, priceA);
                });
                break;
            case BEST_SELLING:
                sorted.sort((a, b) -> Integer.compare(b.getSoldCount(), a.getSoldCount()));
                break;
            case NEWEST:
            default:
                // Đã orderBy createdAt DESC sẵn từ Repository, giữ nguyên thứ tự.
                break;
        }

        // Join author names
        for (Book b : sorted) {
            if (b.getAuthorIds() != null && !b.getAuthorIds().isEmpty()) {
                List<String> authorNames = new ArrayList<>();
                for (String authorId : b.getAuthorIds()) {
                    for (Author author : cachedAuthorList) {
                        if (authorId.equals(author.getAuthorId())) {
                            authorNames.add(author.getName());
                            break;
                        }
                    }
                }
                b.setAuthorNameDisplay(String.join(", ", authorNames));
            } else {
                b.setAuthorNameDisplay("Không rõ tác giả");
            }
        }

        displayedBooks.setValue(sorted);
    }

    private interface BookPredicate {
        boolean test(Book book);
    }

    private List<Book> filterBy(List<Book> source, BookPredicate predicate) {
        List<Book> result = new ArrayList<>();
        for (Book b : source) {
            if (predicate.test(b)) result.add(b);
        }
        return result;
    }

    /** Bật/tắt nhanh từ sw_book_active trong item — không cần mở AddEditBookActivity. */
    public void toggleBookActive(String bookId, boolean isActive) {
        repository.toggleActive(bookId, isActive, new FirebaseCallback<Void>() {
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

    /** Nút xóa trong menu 3 chấm — thực chất là xóa mềm (xem AdminBookRepository.softDeleteBook). */
    public void deleteBook(String bookId) {
        repository.softDeleteBook(bookId, new FirebaseCallback<Void>() {
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
