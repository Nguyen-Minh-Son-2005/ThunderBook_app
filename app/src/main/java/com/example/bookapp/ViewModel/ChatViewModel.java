package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.ChatMessage;
import com.example.bookapp.Repository.ChatRepository;
import com.example.bookapp.Utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ViewModel quản lý trạng thái hội thoại và logic gửi/nhận tin nhắn với Chatbot AI.
 */
public class ChatViewModel extends ViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isTypingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private String sessionId;
    private Book targetBook;
    private String authorsText = "";
    private String categoriesText = "";

    public ChatViewModel() {
        this.repository = new ChatRepository();
        this.sessionId = "session_" + UUID.randomUUID().toString().substring(0, 8);
        loadChatHistory();
    }

    /**
     * Cài đặt ngữ cảnh tư vấn cho một cuốn sách cụ thể.
     */
    public void setTargetBook(Book book, String authors, String categories) {
        if (book == null) return;
        boolean sameBook = (this.targetBook != null && book.getBookId() != null && book.getBookId().equals(this.targetBook.getBookId()));
        this.targetBook = book;
        this.authorsText = authors != null ? authors : "";
        this.categoriesText = categories != null ? categories : "";
        if (sameBook) {
            return;
        }
        if (book.getBookId() != null) {
            this.sessionId = "book_" + book.getBookId() + "_" + UUID.randomUUID().toString().substring(0, 8);
        }
        loadChatHistory();
    }

    public Book getTargetBook() {
        return targetBook;
    }

    private void initWelcomeMessage() {
        List<ChatMessage> list = new ArrayList<>();
        String welcomeText;
        if (targetBook != null) {
            String authorPart = (authorsText != null && !authorsText.isEmpty()) ? " (" + authorsText + ")" : "";
            welcomeText = "Xin chào! Mình là Trợ lý AI của nhà sách Thunder Book ⚡📚\n\n" +
                    "Bạn đang xem cuốn **\"" + targetBook.getTitle() + "\"**" + authorPart + ". " +
                    "Mình có thể giúp bạn tóm tắt nội dung, phân tích đối tượng phù hợp, chỉ ra các bài học đắt giá và giải đáp mọi câu hỏi về cuốn sách này.\n\n" +
                    "Hãy chọn các gợi ý nhanh bên dưới hoặc nhập câu hỏi của bạn nhé!";
        } else {
            welcomeText = "Xin chào! Tôi là Trợ lý Tư vấn Sách AI của nhà sách Thunder Book ⚡📚\n\n" +
                    "Tôi có thể giúp bạn tìm sách theo sở thích, tâm trạng, gợi ý sách bán chạy, kiểm tra giá ưu đãi và tồn kho. Bạn đang muốn tìm cuốn sách như thế nào?";
        }
        ChatMessage welcomeMsg = new ChatMessage(welcomeText, ChatMessage.TYPE_BOT);
        list.add(welcomeMsg);
        messagesLiveData.setValue(list);
    }

    /**
     * Nạp lại lịch sử chat đã lưu từ Cloud Firestore khi mở màn hình.
     */
    public void loadChatHistory() {
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid != null && !uid.trim().isEmpty()) {
            if (targetBook != null && targetBook.getBookId() != null) {
                repository.loadBookChatHistory(uid, targetBook.getBookId(), history -> {
                    if (history != null && !history.isEmpty()) {
                        messagesLiveData.setValue(new ArrayList<>(history));
                    } else {
                        initWelcomeMessage();
                    }
                });
            } else {
                repository.loadChatHistory(uid, history -> {
                    if (history != null && !history.isEmpty()) {
                        messagesLiveData.setValue(new ArrayList<>(history));
                    } else {
                        initWelcomeMessage();
                    }
                });
            }
        } else {
            initWelcomeMessage();
        }
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messagesLiveData;
    }

    public LiveData<Boolean> getIsTyping() {
        return isTypingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public void sendMessage(String userText) {
        if (userText == null || userText.trim().isEmpty()) return;

        String cleanText = userText.trim();
        List<ChatMessage> currentList = messagesLiveData.getValue();
        if (currentList == null) currentList = new ArrayList<>();

        String uid = FirebaseUtils.getCurrentUserId();

        // 1. Thêm tin nhắn của User
        ChatMessage userMessage = new ChatMessage(cleanText, ChatMessage.TYPE_USER);
        currentList.add(userMessage);

        // Lưu tin nhắn User vào Firestore
        if (targetBook != null && targetBook.getBookId() != null) {
            repository.saveBookChatMessage(uid, targetBook.getBookId(), userMessage);
        } else {
            repository.saveChatMessage(uid, userMessage);
        }

        // 2. Thêm tin nhắn Loading Typing của Bot
        ChatMessage loadingMessage = ChatMessage.createLoadingMessage();
        currentList.add(loadingMessage);
        messagesLiveData.setValue(new ArrayList<>(currentList));
        isTypingLiveData.setValue(true);

        // 3. Gửi lên Chatbot Server (kèm ngữ cảnh sách nếu có)
        ChatRepository.ChatCallback callback = new ChatRepository.ChatCallback() {
            @Override
            public void onSuccess(ChatMessage botResponse) {
                List<ChatMessage> list = messagesLiveData.getValue();
                if (list != null && !list.isEmpty()) {
                    // Xóa tin nhắn loading cuối cùng
                    if (list.get(list.size() - 1).isLoading()) {
                        list.remove(list.size() - 1);
                    }
                    // Thêm tin nhắn bot trả về
                    list.add(botResponse);
                    messagesLiveData.setValue(new ArrayList<>(list));

                    // Lưu tin nhắn Bot vào Firestore
                    if (targetBook != null && targetBook.getBookId() != null) {
                        repository.saveBookChatMessage(uid, targetBook.getBookId(), botResponse);
                    } else {
                        repository.saveChatMessage(uid, botResponse);
                    }
                }
                isTypingLiveData.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                List<ChatMessage> list = messagesLiveData.getValue();
                if (list != null && !list.isEmpty()) {
                    // Xóa tin nhắn loading cuối cùng
                    if (list.get(list.size() - 1).isLoading()) {
                        list.remove(list.size() - 1);
                    }
                    // Thêm thông báo lỗi
                    ChatMessage errorBotMsg = new ChatMessage(
                            "⚠️ " + errorMessage,
                            ChatMessage.TYPE_BOT
                    );
                    list.add(errorBotMsg);
                    messagesLiveData.setValue(new ArrayList<>(list));
                }
                isTypingLiveData.setValue(false);
                errorMessageLiveData.setValue(errorMessage);
            }
        };

        if (targetBook != null) {
            repository.sendMessage(uid, sessionId, cleanText, targetBook, authorsText, categoriesText, callback);
        } else {
            repository.sendMessage(uid, sessionId, cleanText, callback);
        }
    }

    public void clearHistory() {
        String uid = FirebaseUtils.getCurrentUserId();
        if (targetBook != null && targetBook.getBookId() != null) {
            repository.clearBookChatHistory(uid, targetBook.getBookId(), () -> {
                this.sessionId = "book_" + targetBook.getBookId() + "_" + UUID.randomUUID().toString().substring(0, 8);
                initWelcomeMessage();
            });
        } else {
            repository.clearChatHistory(uid, () -> {
                this.sessionId = "session_" + UUID.randomUUID().toString().substring(0, 8);
                initWelcomeMessage();
            });
        }
    }
}
