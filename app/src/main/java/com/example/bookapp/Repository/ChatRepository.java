package com.example.bookapp.Repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.ChatMessage;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ChatRepository: Tích hợp trực tiếp Google Gemini AI và Cloud Firestore trên Android.
 * Hỗ trợ Agent Loop (Multi-turn Tool Calling) và thuật toán lọc sách thông minh.
 */
public class ChatRepository {

    private static final String TAG = "ChatRepository";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_TURNS = 5;
    private static final int MAX_HISTORY_TURNS = 20;

    // Google Gemini Embedding Model (Semantic Vector Search 3072 dimensions)
    private static final String EMBEDDING_MODEL = "gemini-embedding-001";
    private static final String EMBEDDING_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
            + EMBEDDING_MODEL + ":embedContent?key=" + Constants.GEMINI_API_KEY;

    private static final String SYSTEM_INSTRUCTION =
            "Bạn là Trợ lý Tư vấn Sách Thông minh của nhà sách Thunder Book.\n" +
            "Nhiệm vụ của bạn là lắng nghe nhu cầu của độc giả, tìm kiếm và tư vấn những cuốn sách phù hợp nhất.\n\n" +
            "QUY TẮC BẮT BUỘC:\n" +
            "1. CHỈ tư vấn và trích xuất dữ liệu dựa trên kết quả trả về từ các công cụ (search_books, check_stock, get_book_detail).\n" +
            "   - Tuyệt đối KHÔNG tự bịa đặt tên sách, giá, tác giả, đánh giá hoặc tình trạng tồn kho.\n" +
            "2. HƯỚNG DẪN CHỌN TOOL:\n" +
            "   - Dùng `search_books`: Khi khách hỏi tìm sách theo tên, tác giả, thể loại, khoảng giá, sách giảm giá, hoặc miêu tả chủ đề/nhu cầu/tâm trạng.\n" +
            "   - Dùng `check_stock`: Khi khách hỏi tình trạng còn hàng / số lượng tồn của 1 cuốn sách.\n" +
            "   - Dùng `get_book_detail`: Khi khách muốn xem chi tiết thông tin đầy đủ về 1 cuốn sách.\n" +
            "3. PHONG CÁCH TƯ VẤN:\n" +
            "   - Thân thiện, chu đáo, nhiệt tình, sử dụng tiếng Việt tự nhiên.\n" +
            "   - Nêu rõ giá khuyến mãi (salePrice), giá gốc (price), điểm đánh giá (Rating) khi tư vấn.\n" +
            "   - Nếu sách hết hàng (stock = 0), hãy thông báo rõ và gợi ý sách tương tự còn hàng.\n" +
            "   - Sau khi tra cứu công cụ có kết quả, luôn tóm tắt và đưa ra câu trả lời đầy đủ, chi tiết cho khách hàng.";

    private final OkHttpClient httpClient;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final List<JSONObject> conversationHistory = new ArrayList<>();
    private final java.util.Map<String, double[]> bookEmbeddingCache = new java.util.concurrent.ConcurrentHashMap<>();

    public ChatRepository() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface ChatCallback {
        void onSuccess(ChatMessage botResponse);
        void onError(String errorMessage);
    }

    /**
     * Gửi tin nhắn đến Google Gemini AI trực tiếp từ Android.
     */
    public void sendMessage(String userId, String sessionId, String userMessage, ChatCallback callback) {
        sendMessage(userId, sessionId, userMessage, null, null, null, callback);
    }

    /**
     * Gửi tin nhắn đến Google Gemini AI kèm ngữ cảnh cuốn sách đang xem.
     */
    public void sendMessage(String userId, String sessionId, String userMessage, Book currentBook, String authorsText, String categoriesText, ChatCallback callback) {
        executorService.execute(() -> {
            try {
                // 1. Giới hạn độ dài lịch sử hội thoại để tránh tràn bộ nhớ/payload quá tải
                if (conversationHistory.size() > MAX_HISTORY_TURNS) {
                    while (conversationHistory.size() > MAX_HISTORY_TURNS) {
                        conversationHistory.remove(0);
                    }
                }

                // 2. Thêm tin nhắn user vào lịch sử hội thoại
                JSONObject userTurn = new JSONObject();
                userTurn.put("role", "user");
                JSONArray userParts = new JSONArray();
                JSONObject textPart = new JSONObject();
                textPart.put("text", userMessage);
                userParts.put(textPart);
                userTurn.put("parts", userParts);
                conversationHistory.add(userTurn);

                List<Book> allSuggestedBooks = new ArrayList<>();
                Set<String> addedBookIds = new HashSet<>();

                String activeSystemInstruction = currentBook != null
                        ? buildBookSystemInstruction(currentBook, authorsText, categoriesText)
                        : SYSTEM_INSTRUCTION;

                // 3. Vòng lặp Agent Multi-Turn Tool Calling
                int turnCount = 0;
                while (turnCount < MAX_TURNS) {
                    turnCount++;

                    JSONObject payload = buildGeminiPayload(activeSystemInstruction);
                    JSONObject geminiResponse = callGeminiApi(payload);

                    if (geminiResponse == null) {
                        postError(callback, "Không thể kết nối đến máy chủ AI (Google Gemini). Vui lòng thử lại sau.");
                        return;
                    }

                    JSONArray candidates = geminiResponse.optJSONArray("candidates");
                    if (candidates == null || candidates.length() == 0) {
                        postError(callback, "Dịch vụ AI phản hồi không có nội dung.");
                        return;
                    }

                    JSONObject candidate = candidates.getJSONObject(0);
                    JSONObject content = candidate.optJSONObject("content");
                    if (content == null) {
                        postError(callback, "Phản hồi AI không đúng định dạng.");
                        return;
                    }

                    JSONArray parts = content.optJSONArray("parts");
                    if (parts == null || parts.length() == 0) {
                        postError(callback, "Dịch vụ AI không trả về dữ liệu.");
                        return;
                    }

                    // Kiểm tra xem có FunctionCall nào không
                    List<JSONObject> functionCalls = new ArrayList<>();
                    StringBuilder textBuffer = new StringBuilder();

                    for (int i = 0; i < parts.length(); i++) {
                        JSONObject p = parts.getJSONObject(i);
                        if (p.has("functionCall")) {
                            functionCalls.add(p.getJSONObject("functionCall"));
                        }
                        if (p.has("text")) {
                            textBuffer.append(p.getString("text"));
                        }
                    }

                    // Trường hợp AI muốn gọi Function (Tool Calling)
                    if (!functionCalls.isEmpty()) {
                        // Thêm model turn vào lịch sử (giữ nguyên parts kèm thought signature)
                        JSONObject modelTurn = new JSONObject();
                        modelTurn.put("role", "model");
                        modelTurn.put("parts", parts);
                        conversationHistory.add(modelTurn);

                        // Thực thi từng function call và gom kết quả
                        JSONArray toolResponseParts = new JSONArray();
                        for (JSONObject fnCall : functionCalls) {
                            String functionName = fnCall.getString("name");
                            JSONObject args = fnCall.optJSONObject("args");
                            if (args == null) args = new JSONObject();

                            Log.d(TAG, "Gemini yêu cầu gọi function [" + functionName + "] với args: " + args);

                            List<Book> turnBooks = new ArrayList<>();
                            JSONObject toolResult = executeFirestoreTool(functionName, args, turnBooks);

                            // Gom sách gợi ý
                            for (Book b : turnBooks) {
                                if (b.getBookId() != null && !addedBookIds.contains(b.getBookId())) {
                                    addedBookIds.add(b.getBookId());
                                    allSuggestedBooks.add(b);
                                }
                            }

                            // Tạo functionResponse part
                            JSONObject fnResponse = new JSONObject();
                            JSONObject fnResponseContent = new JSONObject();
                            fnResponseContent.put("name", functionName);
                            JSONObject respObj = new JSONObject();
                            respObj.put("result", toolResult);
                            fnResponseContent.put("response", respObj);
                            fnResponse.put("functionResponse", fnResponseContent);
                            toolResponseParts.put(fnResponse);
                        }

                        // Thêm kết quả tool vào history
                        JSONObject toolTurn = new JSONObject();
                        toolTurn.put("role", "user");
                        toolTurn.put("parts", toolResponseParts);
                        conversationHistory.add(toolTurn);

                        // Tiếp tục vòng lặp để Gemini đọc kết quả tool và sinh câu trả lời
                        continue;
                    }

                    // Trường hợp AI trả về tin nhắn văn bản hoàn chỉnh
                    String finalReply = textBuffer.toString().trim();
                    if (finalReply.isEmpty() && !allSuggestedBooks.isEmpty()) {
                        finalReply = "Dưới đây là một số tựa sách phù hợp nhất tại nhà sách Thunder Book dành cho bạn:";
                    } else if (finalReply.isEmpty()) {
                        finalReply = "Xin lỗi bạn, tôi chưa tìm thấy thông tin sách phù hợp trong kho. Bạn hãy thử tìm với từ khóa hoặc thể loại khác nhé!";
                    }

                    // Lưu câu trả lời của model vào history
                    JSONObject finalModelTurn = new JSONObject();
                    finalModelTurn.put("role", "model");
                    JSONArray finalParts = new JSONArray();
                    JSONObject finalPart = new JSONObject();
                    finalPart.put("text", finalReply);
                    finalParts.put(finalPart);
                    finalModelTurn.put("parts", finalParts);
                    conversationHistory.add(finalModelTurn);

                    ChatMessage botMsg = new ChatMessage(finalReply, ChatMessage.TYPE_BOT, allSuggestedBooks);
                    mainHandler.post(() -> callback.onSuccess(botMsg));
                    return;
                }

                // Nếu chạy hết số lượt lặp mà chưa có text
                if (!allSuggestedBooks.isEmpty()) {
                    ChatMessage botMsg = new ChatMessage("Dưới đây là các cuốn sách phù hợp nhất tôi đã tìm thấy trong kho:", ChatMessage.TYPE_BOT, allSuggestedBooks);
                    mainHandler.post(() -> callback.onSuccess(botMsg));
                } else {
                    postError(callback, "AI mất nhiều thời gian xử lý. Vui lòng thử lại với câu hỏi ngắn gọn hơn.");
                }

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi xử lý Chatbot: ", e);
                postError(callback, "Đã xảy ra lỗi khi kết nối với AI: " + e.getMessage());
            }
        });
    }

    public interface HistoryCallback {
        void onLoaded(List<ChatMessage> history);
    }

    /**
     * Nạp lịch sử hội thoại đã lưu từ Cloud Firestore cho tài khoản người dùng hiện tại.
     */
    public void loadChatHistory(String userId, HistoryCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            mainHandler.post(() -> callback.onLoaded(new ArrayList<>()));
            return;
        }

        executorService.execute(() -> {
            try {
                QuerySnapshot snapshot = Tasks.await(
                        FirebaseUtils.getFirestore()
                                .collection(Constants.COLLECTION_USERS)
                                .document(userId)
                                .collection("chat_history")
                                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                                .limit(50)
                                .get(),
                        10, TimeUnit.SECONDS
                );

                List<ChatMessage> list = new ArrayList<>();
                conversationHistory.clear();

                for (QueryDocumentSnapshot doc : snapshot) {
                    ChatMessage msg = doc.toObject(ChatMessage.class);
                    if (msg != null) {
                        msg.setMessageId(doc.getId());
                        list.add(msg);

                        // Đồng bộ ngữ cảnh hội thoại vào bộ nhớ Gemini
                        if (msg.getText() != null && !msg.getText().isEmpty()) {
                            JSONObject turn = new JSONObject();
                            turn.put("role", msg.isUser() ? "user" : "model");
                            JSONArray parts = new JSONArray();
                            JSONObject p = new JSONObject();
                            p.put("text", msg.getText());
                            parts.put(p);
                            turn.put("parts", parts);
                            conversationHistory.add(turn);
                        }
                    }
                }

                mainHandler.post(() -> callback.onLoaded(list));
            } catch (Exception e) {
                Log.e(TAG, "Lỗi nạp lịch sử chat từ Firestore: ", e);
                mainHandler.post(() -> callback.onLoaded(new ArrayList<>()));
            }
        });
    }

    /**
     * Lưu tin nhắn (User hoặc Bot) vào Cloud Firestore của người dùng.
     */
    public void saveChatMessage(String userId, ChatMessage message) {
        if (userId == null || userId.trim().isEmpty() || message == null || message.isLoading()) {
            return;
        }

        executorService.execute(() -> {
            try {
                String msgId = message.getMessageId();
                if (msgId == null || msgId.isEmpty()) {
                    msgId = "msg_" + message.getTimestamp() + "_" + (message.isUser() ? "usr" : "bot");
                    message.setMessageId(msgId);
                }

                FirebaseUtils.getFirestore()
                        .collection(Constants.COLLECTION_USERS)
                        .document(userId)
                        .collection("chat_history")
                        .document(msgId)
                        .set(message);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi lưu tin nhắn chat lên Firestore: ", e);
            }
        });
    }

    /**
     * Xóa toàn bộ lịch sử tin nhắn của người dùng trên Cloud Firestore.
     */
    public void clearChatHistory(String userId, Runnable onComplete) {
        conversationHistory.clear();
        if (userId == null || userId.trim().isEmpty()) {
            if (onComplete != null) mainHandler.post(onComplete);
            return;
        }

        executorService.execute(() -> {
            try {
                QuerySnapshot snapshot = Tasks.await(
                        FirebaseUtils.getFirestore()
                                .collection(Constants.COLLECTION_USERS)
                                .document(userId)
                                .collection("chat_history")
                                .get(),
                        10, TimeUnit.SECONDS
                );

                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    doc.getReference().delete();
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi xóa lịch sử chat: ", e);
            }
            if (onComplete != null) mainHandler.post(onComplete);
        });
    }

    /**
     * Xóa lịch sử phiên hội thoại hiện tại trong bộ nhớ RAM.
     */
    public void resetSession(String sessionId) {
        conversationHistory.clear();
    }

    /**
     * Xây dựng System Instruction chuyên sâu cho cuốn sách đang xem.
     */
    private String buildBookSystemInstruction(Book book, String authors, String categories) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là Trợ lý AI Tư vấn Chuyên sâu của nhà sách Thunder Book ⚡📚.\n");
        sb.append("Hiện tại, độc giả đang xem trang chi tiết và cần tư vấn về cuốn sách sau:\n");
        sb.append("- Tên sách: ").append(book.getTitle()).append("\n");
        if (authors != null && !authors.isEmpty()) {
            sb.append("- ").append(authors).append("\n");
        }
        if (categories != null && !categories.isEmpty()) {
            sb.append("- Thể loại: ").append(categories).append("\n");
        }
        double effectivePrice = book.isOnSale() ? book.getSalePrice() : book.getPrice();
        sb.append("- Giá bán: ").append(String.format(java.util.Locale.getDefault(), "%,.0fđ", effectivePrice));
        if (book.isOnSale()) {
            sb.append(" (Giá gốc: ").append(String.format(java.util.Locale.getDefault(), "%,.0fđ", book.getPrice())).append(")");
        }
        sb.append("\n");
        sb.append("- Đánh giá: ").append(book.getRating()).append(" sao (").append(book.getRatingCount()).append(" lượt đánh giá)\n");
        sb.append("- Tình trạng tồn kho: ").append(book.getStock() > 0 ? "Còn hàng (" + book.getStock() + " cuốn)" : "Tạm hết hàng").append("\n");
        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            sb.append("- Giới thiệu tóm tắt từ nhà sách: ").append(book.getDescription()).append("\n");
        }
        sb.append("\nQUY TẮC VÀ NHIỆM VỤ:\n");
        sb.append("1. Trả lời chi tiết, sâu sắc, hấp dẫn và khách quan về nội dung, bài học đắt giá, điểm nổi bật hoặc đối tượng độc giả phù hợp của cuốn sách này.\n");
        sb.append("2. Sử dụng kiến thức phong phú kết hợp với thông tin sách ở trên để giải đáp thắc mắc, giúp độc giả có quyết định mua hoặc đọc sách đúng đắn nhất.\n");
        sb.append("3. Giọng điệu nhiệt tình, thân thiện, truyền cảm hứng đọc sách, sử dụng tiếng Việt tự nhiên và định dạng markdown rõ ràng (in đậm, danh sách gạch đầu dòng).\n");
        sb.append("4. Nếu độc giả hỏi tìm thêm các cuốn sách tương tự, cùng chủ đề, hoặc so sánh với sách khác, hãy sử dụng công cụ `search_books` để tìm kiếm trong kho sách của Thunder Book.");
        return sb.toString();
    }

    /**
     * Nạp lịch sử hội thoại của riêng cuốn sách này từ Firestore.
     */
    public void loadBookChatHistory(String userId, String bookId, HistoryCallback callback) {
        if (userId == null || userId.trim().isEmpty() || bookId == null || bookId.trim().isEmpty()) {
            mainHandler.post(() -> callback.onLoaded(new ArrayList<>()));
            return;
        }

        executorService.execute(() -> {
            try {
                QuerySnapshot snapshot = Tasks.await(
                        FirebaseUtils.getFirestore()
                                .collection(Constants.COLLECTION_USERS)
                                .document(userId)
                                .collection("book_chats")
                                .document(bookId)
                                .collection("messages")
                                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                                .limit(50)
                                .get(),
                        10, TimeUnit.SECONDS
                );

                List<ChatMessage> list = new ArrayList<>();
                conversationHistory.clear();

                for (QueryDocumentSnapshot doc : snapshot) {
                    ChatMessage msg = doc.toObject(ChatMessage.class);
                    if (msg != null) {
                        msg.setMessageId(doc.getId());
                        list.add(msg);

                        if (msg.getText() != null && !msg.getText().isEmpty()) {
                            JSONObject turn = new JSONObject();
                            turn.put("role", msg.isUser() ? "user" : "model");
                            JSONArray parts = new JSONArray();
                            JSONObject p = new JSONObject();
                            p.put("text", msg.getText());
                            parts.put(p);
                            turn.put("parts", parts);
                            conversationHistory.add(turn);
                        }
                    }
                }

                mainHandler.post(() -> callback.onLoaded(list));
            } catch (Exception e) {
                Log.e(TAG, "Lỗi nạp lịch sử book chat từ Firestore: ", e);
                mainHandler.post(() -> callback.onLoaded(new ArrayList<>()));
            }
        });
    }

    /**
     * Lưu tin nhắn hội thoại của riêng cuốn sách này lên Firestore.
     */
    public void saveBookChatMessage(String userId, String bookId, ChatMessage message) {
        if (userId == null || userId.trim().isEmpty() || bookId == null || bookId.trim().isEmpty() || message == null || message.isLoading()) {
            return;
        }

        executorService.execute(() -> {
            try {
                String msgId = message.getMessageId();
                if (msgId == null || msgId.isEmpty()) {
                    msgId = "msg_" + message.getTimestamp() + "_" + (message.isUser() ? "usr" : "bot");
                    message.setMessageId(msgId);
                }

                FirebaseUtils.getFirestore()
                        .collection(Constants.COLLECTION_USERS)
                        .document(userId)
                        .collection("book_chats")
                        .document(bookId)
                        .collection("messages")
                        .document(msgId)
                        .set(message);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi lưu tin nhắn book chat lên Firestore: ", e);
            }
        });
    }

    /**
     * Xóa lịch sử tư vấn riêng của cuốn sách này.
     */
    public void clearBookChatHistory(String userId, String bookId, Runnable onComplete) {
        conversationHistory.clear();
        if (userId == null || userId.trim().isEmpty() || bookId == null || bookId.trim().isEmpty()) {
            if (onComplete != null) mainHandler.post(onComplete);
            return;
        }

        executorService.execute(() -> {
            try {
                QuerySnapshot snapshot = Tasks.await(
                        FirebaseUtils.getFirestore()
                                .collection(Constants.COLLECTION_USERS)
                                .document(userId)
                                .collection("book_chats")
                                .document(bookId)
                                .collection("messages")
                                .get(),
                        10, TimeUnit.SECONDS
                );

                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    doc.getReference().delete();
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi xóa lịch sử book chat: ", e);
            }
            if (onComplete != null) mainHandler.post(onComplete);
        });
    }

    private JSONObject buildGeminiPayload(String systemInstructionText) throws Exception {
        JSONObject payload = new JSONObject();

        // Contents
        JSONArray contents = new JSONArray();
        for (JSONObject turn : conversationHistory) {
            contents.put(turn);
        }
        payload.put("contents", contents);

        // System Instruction
        JSONObject sysInstruction = new JSONObject();
        JSONArray sysParts = new JSONArray();
        JSONObject sysTextPart = new JSONObject();
        sysTextPart.put("text", systemInstructionText != null ? systemInstructionText : SYSTEM_INSTRUCTION);
        sysParts.put(sysTextPart);
        sysInstruction.put("parts", sysParts);
        payload.put("system_instruction", sysInstruction);

        // Tools
        payload.put("tools", getToolsDeclaration());

        return payload;
    }

    private JSONObject callGeminiApi(JSONObject payload) throws IOException {
        RequestBody requestBody = RequestBody.create(payload.toString(), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(Constants.GEMINI_API_URL)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String err = response.body() != null ? response.body().string() : "";
                Log.e(TAG, "Gemini HTTP Error " + response.code() + ": " + err);
                return null;
            }
            if (response.body() == null) return null;
            return new JSONObject(response.body().string());
        } catch (Exception e) {
            Log.e(TAG, "Lỗi gọi HTTP Gemini: ", e);
            return null;
        }
    }

    /**
     * Thực thi truy vấn dữ liệu sách trực tiếp từ Cloud Firestore.
     */
    private JSONObject executeFirestoreTool(String functionName, JSONObject args, List<Book> outSuggestedBooks) {
        JSONObject result = new JSONObject();
        try {
            if ("search_books".equals(functionName)) {
                String keyword = args.optString("keyword", "").trim();
                String genre = args.optString("genre", "").trim();
                double maxPrice = args.optDouble("max_price", -1);
                double minRating = args.optDouble("min_rating", -1);
                boolean onSale = args.optBoolean("on_sale", false);

                // Lấy sách từ Firestore (có timeout 10 giây)
                QuerySnapshot snapshot;
                try {
                    snapshot = Tasks.await(
                            FirebaseUtils.getFirestore()
                                    .collection(Constants.COLLECTION_BOOKS)
                                    .whereEqualTo(Constants.FIELD_IS_ACTIVE, true)
                                    .limit(80)
                                    .get(),
                            10, TimeUnit.SECONDS
                    );
                } catch (Exception e) {
                    // Fallback nếu query isActive gặp lỗi
                    snapshot = Tasks.await(
                            FirebaseUtils.getFirestore()
                                    .collection(Constants.COLLECTION_BOOKS)
                                    .limit(80)
                                    .get(),
                            10, TimeUnit.SECONDS
                    );
                }

                // 1. Tạo Embedding Vector cho từ khóa truy vấn (Semantic Vector Search)
                double[] queryVector = null;
                if (!keyword.isEmpty()) {
                    queryVector = getEmbeddingVector(keyword);
                }

                List<BookMatch> candidateBooks = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    Book book = doc.toObject(Book.class);
                    book.setBookId(doc.getId());

                    // Lọc theo giá tối đa
                    double effectivePrice = book.getSalePrice() > 0 ? book.getSalePrice() : book.getPrice();
                    if (maxPrice > 0 && effectivePrice > maxPrice) {
                        continue;
                    }

                    // Lọc theo điểm đánh giá tối thiểu
                    if (minRating > 0 && book.getRating() < minRating) {
                        continue;
                    }

                    // Lọc sách giảm giá
                    if (onSale && !book.isOnSale()) {
                        continue;
                    }

                    // Điểm ban đầu (Lexical + Cached Embedding nếu có)
                    String cacheKey = book.getBookId() != null ? book.getBookId() : book.getTitle();
                    double[] bookVector = cacheKey != null ? bookEmbeddingCache.get(cacheKey) : null;
                    double initialScore = calculateMatchScore(book, keyword, genre, queryVector, bookVector);
                    candidateBooks.add(new BookMatch(book, initialScore));
                }

                // Sắp xếp sơ bộ các ứng viên tiềm năng nhất
                Collections.sort(candidateBooks, (a, b) -> Double.compare(b.score, a.score));

                // 2. Chỉ tính thêm Embedding cho top 5 ứng viên chưa có trong cache để tối ưu tốc độ và không vượt rate limit
                List<BookMatch> finalScoredBooks = new ArrayList<>();
                int topCheckLimit = Math.min(8, candidateBooks.size());
                for (int i = 0; i < topCheckLimit; i++) {
                    Book book = candidateBooks.get(i).book;
                    String cacheKey = book.getBookId() != null ? book.getBookId() : book.getTitle();
                    double[] bookVector = cacheKey != null ? bookEmbeddingCache.get(cacheKey) : null;

                    if (queryVector != null && bookVector == null) {
                        String bookText = (book.getTitle() != null ? book.getTitle() : "") + ". "
                                + (book.getDescription() != null ? book.getDescription() : "");
                        if (book.getTags() != null && !book.getTags().isEmpty()) {
                            bookText += " Thể loại: " + String.join(", ", book.getTags());
                        }
                        bookVector = getEmbeddingVector(bookText);
                        if (bookVector != null && cacheKey != null) {
                            bookEmbeddingCache.put(cacheKey, bookVector);
                        }
                    }

                    double finalScore = calculateMatchScore(book, keyword, genre, queryVector, bookVector);
                    if (finalScore > 0) {
                        finalScoredBooks.add(new BookMatch(book, finalScore));
                    }
                }

                // Sắp xếp lại theo điểm chính xác nhất
                Collections.sort(finalScoredBooks, (a, b) -> Double.compare(b.score, a.score));

                JSONArray booksArray = new JSONArray();
                int limit = Math.min(5, finalScoredBooks.size());
                for (int i = 0; i < limit; i++) {
                    Book b = finalScoredBooks.get(i).book;
                    outSuggestedBooks.add(b);

                    JSONObject bObj = new JSONObject();
                    bObj.put("id", b.getBookId());
                    bObj.put("title", b.getTitle());
                    bObj.put("price", b.getPrice());
                    bObj.put("salePrice", b.getSalePrice());
                    bObj.put("stock", b.getStock());
                    bObj.put("rating", b.getRating());
                    bObj.put("soldCount", b.getSoldCount());
                    bObj.put("description", b.getDescription());
                    bObj.put("coverImageUrl", b.getCoverImageUrl());
                    booksArray.put(bObj);
                }

                result.put("total_found", finalScoredBooks.size());
                result.put("books", booksArray);

            } else if ("check_stock".equals(functionName)) {
                String bookId = args.optString("book_id", "");
                if (bookId.isEmpty()) {
                    result.put("error", "Vui lòng cung cấp mã book_id.");
                    return result;
                }

                DocumentSnapshot doc = Tasks.await(
                        FirebaseUtils.getFirestore()
                                .collection(Constants.COLLECTION_BOOKS)
                                .document(bookId)
                                .get(),
                        10, TimeUnit.SECONDS
                );

                if (doc.exists()) {
                    Book book = doc.toObject(Book.class);
                    if (book != null) {
                        book.setBookId(doc.getId());
                        outSuggestedBooks.add(book);

                        result.put("id", book.getBookId());
                        result.put("title", book.getTitle());
                        result.put("stock", book.getStock());
                        result.put("status", book.getStock() > 0 ? "Còn hàng" : "Tạm hết hàng");
                        result.put("price", book.getPrice());
                        result.put("salePrice", book.getSalePrice());
                    }
                } else {
                    result.put("error", "Không tìm thấy sách với mã " + bookId);
                }

            } else if ("get_book_detail".equals(functionName)) {
                String bookId = args.optString("book_id", "");
                DocumentSnapshot doc = Tasks.await(
                        FirebaseUtils.getFirestore()
                                .collection(Constants.COLLECTION_BOOKS)
                                .document(bookId)
                                .get(),
                        10, TimeUnit.SECONDS
                );

                if (doc.exists()) {
                    Book book = doc.toObject(Book.class);
                    if (book != null) {
                        book.setBookId(doc.getId());
                        outSuggestedBooks.add(book);

                        result.put("id", book.getBookId());
                        result.put("title", book.getTitle());
                        result.put("price", book.getPrice());
                        result.put("salePrice", book.getSalePrice());
                        result.put("stock", book.getStock());
                        result.put("rating", book.getRating());
                        result.put("description", book.getDescription());
                    }
                } else {
                    result.put("error", "Không tìm thấy sách với mã " + bookId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi truy vấn Firestore trong Tool: ", e);
            try {
                result.put("error", "Lỗi truy vấn Firestore: " + e.getMessage());
            } catch (Exception ignored) {}
        }
        return result;
    }

    /**
     * Gọi Google Gemini Embedding Model (gemini-embedding-001) để biến văn bản thành Vector 3072 chiều.
     */
    public double[] getEmbeddingVector(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            JSONObject contentObj = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", text.trim());
            partsArray.put(part);
            contentObj.put("parts", partsArray);

            JSONObject payload = new JSONObject();
            payload.put("model", "models/" + EMBEDDING_MODEL);
            payload.put("content", contentObj);

            RequestBody requestBody = RequestBody.create(payload.toString(), JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(EMBEDDING_API_URL)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "Embedding API warning code: " + response.code());
                    return null;
                }
                JSONObject resObj = new JSONObject(response.body().string());
                JSONObject embeddingObj = resObj.optJSONObject("embedding");
                if (embeddingObj != null) {
                    JSONArray values = embeddingObj.optJSONArray("values");
                    if (values != null && values.length() > 0) {
                        double[] vector = new double[values.length()];
                        for (int i = 0; i < values.length(); i++) {
                            vector[i] = values.getDouble(i);
                        }
                        return vector;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Không thể lấy Embedding Vector (sẽ fallback sang Lexical match): " + e.getMessage());
        }
        return null;
    }

    /**
     * Tính toán khoảng cách Cosine Similarity giữa 2 Vector Embedding (0.0 -> 1.0).
     */
    public double calculateCosineSimilarity(double[] v1, double[] v2) {
        if (v1 == null || v2 == null || v1.length == 0 || v2.length == 0 || v1.length != v2.length) {
            return 0.0;
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        if (normA <= 0 || normB <= 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Thuật toán Hybrid Search: Kết hợp Vector Semantic Embedding và Lexical Keyword tiếng Việt.
     */
    private double calculateMatchScore(Book book, String keyword, String genre, double[] queryVector, double[] bookVector) {
        if ((keyword == null || keyword.isEmpty()) && (genre == null || genre.isEmpty())) {
            return 1.0 + (book.getRating() * 2) + (book.getSoldCount() * 0.05);
        }

        double score = 0.0;

        // 1. Điểm tương đồng ngữ nghĩa Vector Embedding (Semantic Similarity)
        if (queryVector != null && bookVector != null) {
            double sim = calculateCosineSimilarity(queryVector, bookVector);
            if (sim > 0.45) {
                // Thang điểm semantic từ 0 -> 45 điểm
                score += (sim - 0.45) * 110.0;
            }
        }

        String normTitle = stripAccents(book.getTitle() != null ? book.getTitle() : "");
        String normDesc = stripAccents(book.getDescription() != null ? book.getDescription() : "");
        
        StringBuilder tagsBuilder = new StringBuilder();
        if (book.getTags() != null) {
            for (String t : book.getTags()) {
                tagsBuilder.append(t).append(" ");
            }
        }
        String normTags = stripAccents(tagsBuilder.toString());

        // 2. Khớp nguyên cụm từ khóa (Phrase match)
        if (keyword != null && !keyword.isEmpty()) {
            String normKeyword = stripAccents(keyword);
            if (normTitle.contains(normKeyword)) {
                score += 50.0;
            }
            if (normTags.contains(normKeyword)) {
                score += 30.0;
            }
            if (normDesc.contains(normKeyword)) {
                score += 20.0;
            }

            // 3. Khớp từng từ đơn (Token match)
            String[] tokens = normKeyword.split("\\s+");
            for (String t : tokens) {
                if (t.length() <= 1) continue;
                if (isStopWord(t)) continue;

                if (normTitle.contains(t)) {
                    score += 12.0;
                }
                if (normTags.contains(t)) {
                    score += 8.0;
                }
                if (normDesc.contains(t)) {
                    score += 3.0;
                }
            }
        }

        // 4. Khớp thể loại (Genre match)
        if (genre != null && !genre.isEmpty()) {
            String normGenre = stripAccents(genre);
            if (normDesc.contains(normGenre) || normTitle.contains(normGenre) || normTags.contains(normGenre)) {
                score += 25.0;
            }
        }

        // 5. Cộng điểm uy tín từ đánh giá & số lượng đã bán
        if (score > 0) {
            score += (book.getRating() * 2.0) + (book.getSoldCount() * 0.05);
        }

        return score;
    }

    private boolean isStopWord(String word) {
        return "va".equals(word) || "cac".equals(word) || "nhung".equals(word) || "cho".equals(word)
                || "toi".equals(word) || "hay".equals(word) || "giup".equals(word) || "bot".equals(word)
                || "cuon".equals(word) || "sach".equals(word) || "nhieu".equals(word) || "ve".equals(word)
                || "la".equals(word) || "mot".equals(word) || "trong".equals(word) || "muon".equals(word)
                || "tim".equals(word) || "co".equals(word);
    }

    private String stripAccents(String s) {
        if (s == null || s.isEmpty()) return "";
        String nfkd = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfkd).replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'd')
                .toLowerCase()
                .trim();
    }

    private static class BookMatch {
        final Book book;
        final double score;

        BookMatch(Book book, double score) {
            this.book = book;
            this.score = score;
        }
    }

    private JSONArray getToolsDeclaration() throws Exception {
        JSONArray tools = new JSONArray();
        JSONObject toolObj = new JSONObject();
        JSONArray fnDeclarations = new JSONArray();

        // 1. search_books
        JSONObject searchFn = new JSONObject();
        searchFn.put("name", "search_books");
        searchFn.put("description", "Tìm kiếm và tra cứu sách trong kho theo từ khóa (tên sách, chủ đề), thể loại, mức giá tối đa, điểm đánh giá hoặc giảm giá.");

        JSONObject searchParams = new JSONObject();
        searchParams.put("type", "object");
        JSONObject searchProps = new JSONObject();

        JSONObject kwProp = new JSONObject();
        kwProp.put("type", "string");
        kwProp.put("description", "Tên sách, tác giả hoặc từ khóa chủ đề (ví dụ: thói quen, tâm lý, chữa lành, kinh tế)");
        searchProps.put("keyword", kwProp);

        JSONObject genreProp = new JSONObject();
        genreProp.put("type", "string");
        genreProp.put("description", "Thể loại sách cụ thể");
        searchProps.put("genre", genreProp);

        JSONObject priceProp = new JSONObject();
        priceProp.put("type", "number");
        priceProp.put("description", "Mức giá tối đa mong muốn (VND)");
        searchProps.put("max_price", priceProp);

        JSONObject ratingProp = new JSONObject();
        ratingProp.put("type", "number");
        ratingProp.put("description", "Điểm đánh giá tối thiểu (1.0 - 5.0)");
        searchProps.put("min_rating", ratingProp);

        JSONObject saleProp = new JSONObject();
        saleProp.put("type", "boolean");
        saleProp.put("description", "Lọc các sách đang có giảm giá");
        searchProps.put("on_sale", saleProp);

        searchParams.put("properties", searchProps);
        searchFn.put("parameters", searchParams);
        fnDeclarations.put(searchFn);

        // 2. check_stock
        JSONObject stockFn = new JSONObject();
        stockFn.put("name", "check_stock");
        stockFn.put("description", "Kiểm tra số lượng tồn kho và giá bán hiện tại của sách.");
        JSONObject stockParams = new JSONObject();
        stockParams.put("type", "object");
        JSONObject stockProps = new JSONObject();
        JSONObject idProp = new JSONObject();
        idProp.put("type", "string");
        idProp.put("description", "Mã book_id của sách");
        stockProps.put("book_id", idProp);
        stockParams.put("properties", stockProps);
        JSONArray requiredStock = new JSONArray();
        requiredStock.put("book_id");
        stockParams.put("required", requiredStock);
        stockFn.put("parameters", stockParams);
        fnDeclarations.put(stockFn);

        // 3. get_book_detail
        JSONObject detailFn = new JSONObject();
        detailFn.put("name", "get_book_detail");
        detailFn.put("description", "Lấy toàn bộ thông tin chi tiết và mô tả của sách theo book_id.");
        JSONObject detailParams = new JSONObject();
        detailParams.put("type", "object");
        JSONObject detailProps = new JSONObject();
        JSONObject dIdProp = new JSONObject();
        dIdProp.put("type", "string");
        dIdProp.put("description", "Mã book_id của sách");
        detailProps.put("book_id", dIdProp);
        detailParams.put("properties", detailProps);
        JSONArray requiredDetail = new JSONArray();
        requiredDetail.put("book_id");
        detailParams.put("required", requiredDetail);
        detailFn.put("parameters", detailParams);
        fnDeclarations.put(detailFn);

        toolObj.put("functionDeclarations", fnDeclarations);
        tools.put(toolObj);
        return tools;
    }

    private void postError(ChatCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
