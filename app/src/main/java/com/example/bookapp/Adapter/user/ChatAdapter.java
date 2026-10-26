package com.example.bookapp.Adapter.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.ChatMessage;
import com.example.bookapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter chính cho màn hình Chatbot, hỗ trợ hiển thị tin nhắn User và Bot kèm danh sách gợi ý sách.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnSuggestedBookClickListener {
        void onBookClick(Book book);
    }

    private final Context context;
    private final List<ChatMessage> messageList;
    private final OnSuggestedBookClickListener bookClickListener;
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter(Context context, List<ChatMessage> messageList, OnSuggestedBookClickListener bookClickListener) {
        this.context = context;
        this.messageList = messageList;
        this.bookClickListener = bookClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        return message.getSenderType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        if (message == null) return;

        String formattedTime = timeFormatter.format(new Date(message.getTimestamp()));

        if (holder instanceof UserViewHolder) {
            UserViewHolder userHolder = (UserViewHolder) holder;
            userHolder.tvUserMessage.setText(message.getText());
            userHolder.tvUserTime.setText(formattedTime);
        } else if (holder instanceof BotViewHolder) {
            BotViewHolder botHolder = (BotViewHolder) holder;

            if (message.isLoading()) {
                botHolder.tvBotMessage.setVisibility(View.GONE);
                botHolder.pbTyping.setVisibility(View.VISIBLE);
                botHolder.rvSuggestedBooks.setVisibility(View.GONE);
                botHolder.tvBotTime.setVisibility(View.GONE);
            } else {
                botHolder.tvBotMessage.setVisibility(View.VISIBLE);
                botHolder.pbTyping.setVisibility(View.GONE);
                botHolder.tvBotMessage.setText(formatMarkdown(message.getText()));
                botHolder.tvBotTime.setVisibility(View.VISIBLE);
                botHolder.tvBotTime.setText(formattedTime);

                // Hiển thị danh sách sách gợi ý nếu có
                if (message.hasSuggestedBooks()) {
                    botHolder.rvSuggestedBooks.setVisibility(View.VISIBLE);
                    botHolder.rvSuggestedBooks.setLayoutManager(
                            new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    );
                    ChatBookSuggestionAdapter suggestionAdapter = new ChatBookSuggestionAdapter(
                            context,
                            message.getSuggestedBooks(),
                            book -> {
                                if (bookClickListener != null) {
                                    bookClickListener.onBookClick(book);
                                }
                            }
                    );
                    botHolder.rvSuggestedBooks.setAdapter(suggestionAdapter);
                } else {
                    botHolder.rvSuggestedBooks.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    private CharSequence formatMarkdown(String text) {
        if (text == null || text.isEmpty()) return "";
        try {
            // Thay thế tiêu đề markdown ### hoặc ##
            String formatted = text.replaceAll("(?m)^#{1,4}\\s*(.*)$", "<b>$1</b>");
            // Thay thế chữ in đậm **text** thành <b>text</b>
            formatted = formatted.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
            // Thay thế dấu gạch đầu dòng * thành bullet •
            formatted = formatted.replaceAll("(?m)^\\s*\\*\\s+", "• ");
            // Thay thế xuống dòng thành <br/>
            formatted = formatted.replace("\n", "<br/>");

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                return android.text.Html.fromHtml(formatted, android.text.Html.FROM_HTML_MODE_COMPACT);
            } else {
                return android.text.Html.fromHtml(formatted);
            }
        } catch (Exception e) {
            return text;
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMessage, tvUserTime;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            tvUserTime = itemView.findViewById(R.id.tv_user_time);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView tvBotMessage, tvBotTime;
        ProgressBar pbTyping;
        RecyclerView rvSuggestedBooks;

        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBotMessage = itemView.findViewById(R.id.tv_bot_message);
            tvBotTime = itemView.findViewById(R.id.tv_bot_time);
            pbTyping = itemView.findViewById(R.id.pb_typing);
            rvSuggestedBooks = itemView.findViewById(R.id.rv_suggested_books);
        }
    }
}
