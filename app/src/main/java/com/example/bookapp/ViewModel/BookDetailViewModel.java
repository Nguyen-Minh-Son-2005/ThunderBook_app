package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Author;
import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.Category;
import com.example.bookapp.Model.Review;
import com.example.bookapp.Repository.AuthorRepository;
import com.example.bookapp.Repository.BookRepository;
import com.example.bookapp.Repository.CartRepository;
import com.example.bookapp.Repository.CategoryRepository;
import com.example.bookapp.Repository.PublisherRepository;
import com.example.bookapp.Repository.ReviewRepository;
import com.example.bookapp.Utils.FirebaseCallback;

import java.util.List;

/**
 * ViewModel cho BookDetailActivity.
 */
public class BookDetailViewModel extends ViewModel {

    private final BookRepository bookRepository = new BookRepository();
    private final CartRepository cartRepository = new CartRepository();
    private final AuthorRepository authorRepository = new AuthorRepository();
    private final PublisherRepository publisherRepository = new PublisherRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final ReviewRepository reviewRepository = new ReviewRepository();

    private final MutableLiveData<Book> _book = new MutableLiveData<>();
    private final MutableLiveData<List<Book>> _relatedBooks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _addToCartSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    
    private final MutableLiveData<String> _authorsText = new MutableLiveData<>();
    private final MutableLiveData<String> _categoriesText = new MutableLiveData<>();
    private final MutableLiveData<List<Review>> _reviews = new MutableLiveData<>();

    public LiveData<Book> getBook() { return _book; }
    public LiveData<List<Book>> getRelatedBooks() { return _relatedBooks; }
    public LiveData<Boolean> getAddToCartSuccess() { return _addToCartSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    
    public LiveData<String> getAuthorsText() { return _authorsText; }
    public LiveData<String> getCategoriesText() { return _categoriesText; }
    public LiveData<List<Review>> getReviews() { return _reviews; }

    public void loadBook(String bookId) {
        bookRepository.getBook(bookId).observeForever(book -> {
            _book.setValue(book);
            if (book != null) {
                loadAuthors(book.getAuthorIds());
                loadCategories(book.getCategoryIds());
                loadReviews(bookId);
            }
        });
    }

    private void loadAuthors(List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            _authorsText.setValue("Chưa cập nhật");
            return;
        }
        authorRepository.getAuthorsByIds(authorIds).observeForever(authors -> {
            if (authors != null && !authors.isEmpty()) {
                StringBuilder sb = new StringBuilder("Tác giả: ");
                for (int i = 0; i < authors.size(); i++) {
                    sb.append(authors.get(i).getName());
                    if (i < authors.size() - 1) sb.append(", ");
                }
                _authorsText.setValue(sb.toString());
            } else {
                _authorsText.setValue("Chưa cập nhật");
            }
        });
    }
    
    private void loadCategories(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            _categoriesText.setValue("Khác");
            return;
        }
        categoryRepository.getCategoriesByIds(categoryIds).observeForever(categories -> {
            if (categories != null && !categories.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < categories.size(); i++) {
                    sb.append(categories.get(i).getName());
                    if (i < categories.size() - 1) sb.append(", ");
                }
                _categoriesText.setValue(sb.toString());
            } else {
                _categoriesText.setValue("Khác");
            }
        });
    }
    
    private void loadReviews(String bookId) {
        reviewRepository.getReviewsByBook(bookId, 5).observeForever(reviews -> {
            _reviews.setValue(reviews);
        });
    }

    public void loadRelatedBooks(String excludeBookId, String categoryId) {
        bookRepository.getRelatedBooks(excludeBookId, categoryId).observeForever(
                books -> _relatedBooks.setValue(books));
    }

    public void addToCart(String uid, String bookId, int quantity, double priceAtAdd) {
        cartRepository.addToCart(uid, bookId, quantity, priceAtAdd, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _addToCartSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.setValue(e.getMessage());
            }
        });
    }

    public void incrementViewCount(String bookId) {
        bookRepository.incrementViewCount(bookId);
    }

    public void resetAddToCartStatus() {
        _addToCartSuccess.setValue(null);
    }
}
