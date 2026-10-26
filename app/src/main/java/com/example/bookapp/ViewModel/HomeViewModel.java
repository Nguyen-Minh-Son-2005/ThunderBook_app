package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.Category;
import com.example.bookapp.Model.User;
import com.example.bookapp.Repository.BookRepository;
import com.example.bookapp.Repository.CategoryRepository;
import com.example.bookapp.Repository.UserRepository;
import com.example.bookapp.Repository.NotificationRepository;
import com.example.bookapp.Model.Notification;

import java.util.List;

/**
 * ViewModel cho HomeFragment.
 * Tách 3 nguồn dữ liệu (users, categories, books) thành 3 LiveData riêng.
 * Fragment chỉ observe — không tự gọi Firestore.
 */
public class HomeViewModel extends ViewModel {

    private final UserRepository userRepository = new UserRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final com.example.bookapp.Repository.AuthorRepository authorRepository = new com.example.bookapp.Repository.AuthorRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();

    private final MutableLiveData<User> _currentUser = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> _categories = new MutableLiveData<>();
    private final MutableLiveData<List<Book>> _featuredBooks = new MutableLiveData<>();
    private final MutableLiveData<List<Book>> _allBooks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _hasUnreadNotification = new MutableLiveData<>();

    public LiveData<User> getCurrentUser() { return _currentUser; }
    public LiveData<List<Category>> getCategories() { return _categories; }
    public LiveData<List<Book>> getFeaturedBooks() { return _featuredBooks; }
    public LiveData<List<Book>> getAllBooks() { return _allBooks; }
    public LiveData<Boolean> getHasUnreadNotification() { return _hasUnreadNotification; }

    // Quản lý Observers để tránh rỉ bộ nhớ
    private LiveData<User> userLiveData;
    private androidx.lifecycle.Observer<User> userObserver;

    private LiveData<List<Category>> categoryLiveData;
    private androidx.lifecycle.Observer<List<Category>> categoryObserver;

    private LiveData<List<Book>> featuredBooksLiveData;
    private androidx.lifecycle.Observer<List<Book>> featuredBooksObserver;

    private LiveData<List<Book>> allBooksLiveData;
    private androidx.lifecycle.Observer<List<Book>> allBooksObserver;

    private LiveData<List<com.example.bookapp.Model.Author>> authorsLiveData;
    private androidx.lifecycle.Observer<List<com.example.bookapp.Model.Author>> authorsObserver;

    private LiveData<List<Notification>> notificationLiveData;
    private androidx.lifecycle.Observer<List<Notification>> notificationObserver;

    // Cache dữ liệu để ghép tên tác giả
    private final java.util.Map<String, String> authorNameMap = new java.util.HashMap<>();
    private List<Book> tempFeaturedBooks;
    private List<Book> tempAllBooks;

    public void load(String uid) {
        loadUser(uid);
        loadCategories();
        loadAuthors(); // Lấy tác giả trước hoặc song song
        loadFeaturedBooks();
        loadAllBooks();
        loadNotifications(uid);
    }

    private void loadNotifications(String uid) {
        if (notificationLiveData != null && notificationObserver != null) {
            notificationLiveData.removeObserver(notificationObserver);
        }
        notificationLiveData = notificationRepository.getNotifications(uid);
        notificationObserver = notifications -> {
            boolean hasUnread = false;
            if (notifications != null) {
                for (Notification n : notifications) {
                    if (!n.isRead()) {
                        hasUnread = true;
                        break;
                    }
                }
            }
            _hasUnreadNotification.setValue(hasUnread);
        };
        notificationLiveData.observeForever(notificationObserver);
    }

    private void loadAuthors() {
        if (authorsLiveData != null && authorsObserver != null) {
            authorsLiveData.removeObserver(authorsObserver);
        }
        authorsLiveData = authorRepository.getAllAuthors();
        authorsObserver = authors -> {
            if (authors != null) {
                for (com.example.bookapp.Model.Author a : authors) {
                    authorNameMap.put(a.getAuthorId(), a.getName());
                }
                updateBookAuthors(tempFeaturedBooks, _featuredBooks);
                updateBookAuthors(tempAllBooks, _allBooks);
            }
        };
        authorsLiveData.observeForever(authorsObserver);
    }

    private void loadUser(String uid) {
        if (userLiveData != null && userObserver != null) {
            userLiveData.removeObserver(userObserver);
        }
        userLiveData = userRepository.getUser(uid);
        userObserver = user -> _currentUser.setValue(user);
        userLiveData.observeForever(userObserver);
    }

    private void loadCategories() {
        if (categoryLiveData != null && categoryObserver != null) {
            categoryLiveData.removeObserver(categoryObserver);
        }
        categoryLiveData = categoryRepository.getActiveCategories();
        categoryObserver = categories -> _categories.setValue(categories);
        categoryLiveData.observeForever(categoryObserver);
    }

    private void loadFeaturedBooks() {
        if (featuredBooksLiveData != null && featuredBooksObserver != null) {
            featuredBooksLiveData.removeObserver(featuredBooksObserver);
        }
        featuredBooksLiveData = bookRepository.getFeaturedBooks();
        featuredBooksObserver = books -> {
            tempFeaturedBooks = books;
            updateBookAuthors(tempFeaturedBooks, _featuredBooks);
        };
        featuredBooksLiveData.observeForever(featuredBooksObserver);
    }

    private void loadAllBooks() {
        if (allBooksLiveData != null && allBooksObserver != null) {
            allBooksLiveData.removeObserver(allBooksObserver);
        }
        allBooksLiveData = bookRepository.getAllActiveBooks();
        allBooksObserver = books -> {
            if (books != null && !books.isEmpty()) {
                // Trộn ngẫu nhiên và lấy tối đa 20 cuốn
                java.util.Collections.shuffle(books);
                tempAllBooks = books.subList(0, Math.min(books.size(), 20));
            } else {
                tempAllBooks = books;
            }
            updateBookAuthors(tempAllBooks, _allBooks);
        };
        allBooksLiveData.observeForever(allBooksObserver);
    }

    private void updateBookAuthors(List<Book> books, MutableLiveData<List<Book>> liveData) {
        if (books != null) {
            if (!authorNameMap.isEmpty()) {
                for (Book book : books) {
                    if (book.getAuthorIds() != null && !book.getAuthorIds().isEmpty()) {
                        java.util.List<String> names = new java.util.ArrayList<>();
                        for (String id : book.getAuthorIds()) {
                            String name = authorNameMap.get(id);
                            if (name != null) names.add(name);
                        }
                        book.setAuthorNameDisplay(android.text.TextUtils.join(", ", names));
                    }
                }
            }
            liveData.setValue(books);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (userLiveData != null && userObserver != null) userLiveData.removeObserver(userObserver);
        if (categoryLiveData != null && categoryObserver != null) categoryLiveData.removeObserver(categoryObserver);
        if (featuredBooksLiveData != null && featuredBooksObserver != null) featuredBooksLiveData.removeObserver(featuredBooksObserver);
        if (allBooksLiveData != null && allBooksObserver != null) allBooksLiveData.removeObserver(allBooksObserver);
        if (authorsLiveData != null && authorsObserver != null) authorsLiveData.removeObserver(authorsObserver);
        if (notificationLiveData != null && notificationObserver != null) notificationLiveData.removeObserver(notificationObserver);
    }
}
