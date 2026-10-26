package com.example.bookapp.View.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.user.BookAdapter;
import com.example.bookapp.Model.Book;
import com.example.bookapp.R;
import com.example.bookapp.Repository.BookRepository;
import com.example.bookapp.Repository.FavoriteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FavoriteBooksActivity extends AppCompatActivity {

    private RecyclerView rvFavoriteBooks;
    private LinearLayout llEmptyState;
    private BookAdapter bookAdapter;
    private final List<Book> favoriteBooksList = new ArrayList<>();
    
    private final BookRepository bookRepository = new BookRepository();
    private List<Book> allActiveBooks = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_books);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvFavoriteBooks = findViewById(R.id.rv_favorite_books);
        llEmptyState = findViewById(R.id.ll_empty_state);

        rvFavoriteBooks.setLayoutManager(new GridLayoutManager(this, 2));
        
        bookAdapter = new BookAdapter(favoriteBooksList, book -> {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getBookId());
            startActivity(intent);
        });
        
        rvFavoriteBooks.setAdapter(bookAdapter);

        bookRepository.getAllActiveBooks().observe(this, books -> {
            if (books != null) {
                allActiveBooks = books;
                updateFavoriteList();
            }
        });

        FavoriteRepository.getFavoriteBookIds().observe(this, ids -> {
            updateFavoriteList();
        });
    }

    private void updateFavoriteList() {
        Set<String> favIds = FavoriteRepository.getFavoriteBookIds().getValue();
        if (favIds == null || allActiveBooks.isEmpty()) {
            favoriteBooksList.clear();
        } else {
            favoriteBooksList.clear();
            for (Book book : allActiveBooks) {
                if (favIds.contains(book.getBookId())) {
                    favoriteBooksList.add(book);
                }
            }
        }

        bookAdapter.notifyDataSetChanged();
        
        if (favoriteBooksList.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvFavoriteBooks.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvFavoriteBooks.setVisibility(View.VISIBLE);
        }
    }
}
