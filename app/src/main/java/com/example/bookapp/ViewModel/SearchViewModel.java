package com.example.bookapp.ViewModel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.Category;
import com.example.bookapp.Repository.BookRepository;
import com.example.bookapp.Repository.CategoryRepository;

import java.util.List;

/**
 * ViewModel cho SearchActivity.
 * Giữ kết quả tìm kiếm qua xoay màn hình, tránh gọi lại Firestore không cần thiết.
 */
public class SearchViewModel extends ViewModel {

    private final BookRepository bookRepository = new BookRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final com.example.bookapp.Repository.AuthorRepository authorRepository = new com.example.bookapp.Repository.AuthorRepository();

    private final MutableLiveData<List<Category>> _categories = new MutableLiveData<>();
    private final MutableLiveData<List<Book>> _searchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    private final java.util.Map<String, String> authorNameMap = new java.util.HashMap<>();
    private List<Book> tempBooks;
    private String currentKeyword = "";
    private boolean isFeaturedOnly = false;

    public void setFeaturedOnly(boolean featuredOnly) {
        this.isFeaturedOnly = featuredOnly;
    }

    public SearchViewModel() {
        authorRepository.getAllAuthors().observeForever(authors -> {
            if (authors != null) {
                for (com.example.bookapp.Model.Author a : authors) {
                    authorNameMap.put(a.getAuthorId(), a.getName());
                }
                if (tempBooks != null) {
                    updateBookAuthorsAndFilter(currentKeyword);
                }
            }
        });
    }

    public LiveData<List<Category>> getCategories() { return _categories; }
    public LiveData<List<Book>> getSearchResults() { return _searchResults; }
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    public void loadCategories() {
        categoryRepository.getActiveCategories().observeForever(
                categories -> _categories.setValue(categories));
    }

    /**
     * Lấy toàn bộ sách theo danh mục (hoặc tất cả), 
     * sau đó filter theo keyword (tên sách / tên tác giả) bằng Java.
     */
    public void searchBooks(String keyword, @Nullable String categoryId, String sortKey) {
        _isLoading.setValue(true);
        currentKeyword = keyword;
        bookRepository.searchBooks(keyword, categoryId, sortKey).observeForever(books -> {
            tempBooks = books;
            updateBookAuthorsAndFilter(keyword);
            _isLoading.setValue(false);
        });
    }

    public void updateKeywordLocal(String keyword) {
        currentKeyword = keyword;
        updateBookAuthorsAndFilter(keyword);
    }

    private void updateBookAuthorsAndFilter(String keyword) {
        if (tempBooks == null) return;
        
        java.util.List<Book> filtered = new java.util.ArrayList<>();
        String kw = keyword.toLowerCase().trim();

        for (Book book : tempBooks) {
            // Ghép tên tác giả
            if (book.getAuthorIds() != null && !book.getAuthorIds().isEmpty() && !authorNameMap.isEmpty()) {
                java.util.List<String> names = new java.util.ArrayList<>();
                for (String id : book.getAuthorIds()) {
                    String name = authorNameMap.get(id);
                    if (name != null) names.add(name);
                }
                book.setAuthorNameDisplay(android.text.TextUtils.join(", ", names));
            }

            // Lọc theo nổi bật
            if (isFeaturedOnly && !book.isFeatured()) {
                continue;
            }

            // Lọc theo tên sách hoặc tác giả (không phân biệt hoa thường và không dấu)
            boolean matches = true;
            if (!kw.isEmpty()) {
                String title = book.getTitle() != null ? book.getTitle().toLowerCase() : "";
                String author = book.getAuthorNameDisplay() != null ? book.getAuthorNameDisplay().toLowerCase() : "";
                
                String unaccentedTitle = com.example.bookapp.Utils.StringUtils.removeAccents(title).toLowerCase();
                String unaccentedAuthor = com.example.bookapp.Utils.StringUtils.removeAccents(author).toLowerCase();
                String unaccentedKw = com.example.bookapp.Utils.StringUtils.removeAccents(kw).toLowerCase();

                matches = title.contains(kw) || author.contains(kw) ||
                          unaccentedTitle.contains(unaccentedKw) || unaccentedAuthor.contains(unaccentedKw);
            }
            if (matches) {
                filtered.add(book);
            }
        }
        _searchResults.setValue(filtered);
    }
}
