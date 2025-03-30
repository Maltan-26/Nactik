package com.example.nactik_chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String CURRENT_TIME = "2025-03-27 18:12:42";
    private  final String CURRENT_USER;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;

    public ChatAdapter(Context context,String CURRENT_USER) {
        this.CURRENT_USER = CURRENT_USER;
        this.context = context;
        this.messageList = new ArrayList<>();
    }

    public void setMessages(List<Message> messages) {
        this.messageList.clear();
        this.messageList.addAll(messages);
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        this.messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.sent_message_layout, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.received_message_layout, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            configureSentMessage((SentMessageHolder) holder, message);
        } else {
            configureReceivedMessage((ReceivedMessageHolder) holder, message);
        }
    }

    private void configureSentMessage(SentMessageHolder holder, Message message) {
        holder.messageText.setText(message.getMessageText());
        holder.timeText.setText(message.getTimeString());
    }

    private void configureReceivedMessage(ReceivedMessageHolder holder, Message message) {
        holder.messageText.setText(message.getMessageText());
        holder.timeText.setText(message.getTimeString());
        holder.senderName.setText(message.getSenderName());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSenderUid().equals(CURRENT_USER)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    // ViewHolder for sent messages
    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sent_message_text);
            timeText = itemView.findViewById(R.id.sent_message_time);
        }
    }

    // ViewHolder for received messages
    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;
        TextView senderName;

        ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.received_message_text);
            timeText = itemView.findViewById(R.id.received_message_time);
            senderName = itemView.findViewById(R.id.message_sender_name);
        }
    }
}