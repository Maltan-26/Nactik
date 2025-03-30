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

public class MessagesAdapter extends RecyclerView.Adapter {
    private static final int ITEM_SEND = 1;
    private static final int ITEM_RECEIVE = 2;

    private Context context;
    private List<Message> messages;
    private String currentUserId;

    public MessagesAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.messages = new ArrayList<>();
    }
    public MessagesAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
        // Get the current user from the activity
        if (context instanceof specificchat) {
            this.currentUserId = ((specificchat) context).getCurrentUser();
        }
    }


    // Alternative constructor if you need to specify currentUserId
    public MessagesAdapter(Context context, ArrayList<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<Message> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.senderchatlayout, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.recieverchatlayout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.bind(message);
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.bind(message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return currentUserId.equals(message.getSenderId()) ? ITEM_SEND : ITEM_RECEIVE;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView messageTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sendermessage);
            messageTime = itemView.findViewById(R.id.timeofmessage);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            messageTime.setText(message.getCurrenttime());
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView messageTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sendermessage);
            messageTime = itemView.findViewById(R.id.timeofmessage);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            messageTime.setText(message.getCurrenttime());
        }
    }
}
