package com.example.bookapp.View.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.user.ChatAdapter;
import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.ChatMessage;
import com.example.bookapp.R;
import com.example.bookapp.ViewModel.ChatViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Chatbot AI tư vấn sách cho độc giả Thunder Book.
 */
public class ChatBotActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";

    private RecyclerView rvChatMessages;
    private EditText etMessageInput;
    private ImageButton ibSendMessage, ibBack, ibClearChat;

    private ChatViewModel chatViewModel;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private TextView tvBotTitle, tvBotSubtitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        bindViews();
        setupRecyclerView();
        setupViewModel();
        setupListeners();

        String targetBookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        if (targetBookId != null && !targetBookId.trim().isEmpty()) {
            setupBookQuickPrompts();
            new com.example.bookapp.Repository.BookRepository().getBook(targetBookId).observe(this, book -> {
                if (book != null) {
                    if (tvBotTitle != null) {
                        tvBotTitle.setText("Tư vấn: " + book.getTitle());
                    }
                    if (tvBotSubtitle != null) {
                        tvBotSubtitle.setText("Trợ lý AI chuyên sâu về sách");
                    }
                    if (etMessageInput != null) {
                        etMessageInput.setHint("Hỏi AI về cuốn sách này...");
                    }
                    chatViewModel.setTargetBook(book, "", "");
                }
            });
        } else {
            setupQuickPrompts();
        }
    }

    private void bindViews() {
        rvChatMessages = findViewById(R.id.rv_chat_messages);
        etMessageInput = findViewById(R.id.et_message_input);
        ibSendMessage = findViewById(R.id.ib_send_message);
        ibBack = findViewById(R.id.ib_back);
        ibClearChat = findViewById(R.id.ib_clear_chat);
        tvBotTitle = findViewById(R.id.tv_bot_title);
        tvBotSubtitle = findViewById(R.id.tv_bot_subtitle);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(this, messageList, this::openBookDetail);
        rvChatMessages.setAdapter(chatAdapter);

        // Tự động cuộn xuống dưới cùng khi bàn phím xuất hiện
        rvChatMessages.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && !messageList.isEmpty()) {
                rvChatMessages.postDelayed(() -> {
                    if (!messageList.isEmpty()) {
                        rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
                    }
                }, 100);
            }
        });
    }

    private void setupViewModel() {
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        chatViewModel.getMessages().observe(this, messages -> {
            if (messages != null) {
                messageList.clear();
                messageList.addAll(messages);
                chatAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
                }
            }
        });

        chatViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        ibBack.setOnClickListener(v -> finish());

        ibSendMessage.setOnClickListener(v -> handleSendMessage());

        etMessageInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !messageList.isEmpty()) {
                rvChatMessages.postDelayed(() -> {
                    if (!messageList.isEmpty()) {
                        rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
                    }
                }, 200);
            }
        });

        etMessageInput.setOnClickListener(v -> {
            if (!messageList.isEmpty()) {
                rvChatMessages.postDelayed(() -> {
                    if (!messageList.isEmpty()) {
                        rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
                    }
                }, 200);
            }
        });

        etMessageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handleSendMessage();
                return true;
            }
            return false;
        });

        ibClearChat.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa lịch sử hội thoại")
                    .setMessage("Bạn có chắc muốn xóa toàn bộ tin nhắn và bắt đầu lại cuộc trò chuyện mới?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        chatViewModel.clearHistory();
                        Toast.makeText(this, "Đã làm mới cuộc hội thoại", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void setupQuickPrompts() {
        findViewById(R.id.chip_prompt_heal).setOnClickListener(v -> sendQuickPrompt("Gợi ý cho tôi những cuốn sách hay giúp chữa lành tâm hồn và bớt lo âu"));
        findViewById(R.id.chip_prompt_habits).setOnClickListener(v -> sendQuickPrompt("Tôi muốn tìm sách hướng dẫn xây dựng và rèn luyện thói quen tốt"));
        findViewById(R.id.chip_prompt_cheap).setOnClickListener(v -> sendQuickPrompt("Có sách nào hay giá dưới 100k đang có sẵn trong kho không?"));
        findViewById(R.id.chip_prompt_bestseller).setOnClickListener(v -> sendQuickPrompt("Những cuốn sách nào đang được độc giả mua nhiều và đánh giá cao nhất?"));
        findViewById(R.id.chip_prompt_business).setOnClickListener(v -> sendQuickPrompt("Tư vấn cho tôi sách về khởi nghiệp, tư duy kinh doanh và làm giàu"));
    }

    private void setupBookQuickPrompts() {
        TextView chip1 = findViewById(R.id.chip_prompt_heal);
        TextView chip2 = findViewById(R.id.chip_prompt_habits);
        TextView chip3 = findViewById(R.id.chip_prompt_cheap);
        TextView chip4 = findViewById(R.id.chip_prompt_bestseller);
        TextView chip5 = findViewById(R.id.chip_prompt_business);

        if (chip1 != null) {
            chip1.setText("💡 Tóm tắt nội dung");
            chip1.setOnClickListener(v -> sendQuickPrompt("Hãy tóm tắt ngắn gọn và cuốn hút nội dung chính của cuốn sách này."));
        }
        if (chip2 != null) {
            chip2.setText("🎯 Phù hợp với ai?");
            chip2.setOnClickListener(v -> sendQuickPrompt("Cuốn sách này phù hợp với đối tượng độc giả nào và ai nên đọc?"));
        }
        if (chip3 != null) {
            chip3.setText("⭐ Bài học đắt giá");
            chip3.setOnClickListener(v -> sendQuickPrompt("Những bài học hoặc giá trị đắt giá nhất mà cuốn sách này mang lại là gì?"));
        }
        if (chip4 != null) {
            chip4.setText("💬 Độc giả đánh giá thế nào?");
            chip4.setOnClickListener(v -> sendQuickPrompt("Độc giả đánh giá thế nào về cuốn sách này? Điểm cộng và điểm trừ là gì?"));
        }
        if (chip5 != null) {
            chip5.setText("💰 Có đáng mua không?");
            chip5.setOnClickListener(v -> sendQuickPrompt("Theo bạn cuốn sách này có đáng mua không, lý do tại sao?"));
        }
    }

    private void sendQuickPrompt(String prompt) {
        chatViewModel.sendMessage(prompt);
    }

    private void handleSendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (!text.isEmpty()) {
            chatViewModel.sendMessage(text);
            etMessageInput.setText("");
        }
    }

    private void openBookDetail(Book book) {
        if (book == null || book.getBookId() == null) return;
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getBookId());
        startActivity(intent);
    }
}
