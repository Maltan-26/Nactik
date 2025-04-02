package com.example.nactik_chat;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {
    private List<ChatRoom> chatRooms = new ArrayList<>();
    private static final String TAG = "ChatRoomAdapter";

    public void setChatRooms(List<ChatRoom> rooms) {
        this.chatRooms = rooms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom room = chatRooms.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatarView;
        private TextView nameText;
        private TextView lastMessageText;
        private TextView timeText;
        private View onlineStatusIndicator;
        private TextView unreadBadge;

        ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatarView);
            nameText = itemView.findViewById(R.id.nameText);
            lastMessageText = itemView.findViewById(R.id.lastMessageText);
            timeText = itemView.findViewById(R.id.timeText);
            onlineStatusIndicator = itemView.findViewById(R.id.onlineStatusIndicator);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);

            // Add click listener to open specific chat
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ChatRoom room = chatRooms.get(position);
                    Intent intent = new Intent(itemView.getContext(), specificchat.class);
                    intent.putExtra("room_id", room.getRoomId());
                    intent.putExtra("receivername", room.getRoomName());
                    intent.putExtra("receiverImage", room.getImageUrl());
                    itemView.getContext().startActivity(intent);
                }
            });
        }

        void bind(ChatRoom room) {
            nameText.setText(room.getRoomName());
            lastMessageText.setText(room.getLastMessage());
            timeText.setText(room.getLastMessageTime());

            // Set online status
            onlineStatusIndicator.setVisibility(
                    room.isOnline() ? View.VISIBLE : View.GONE
            );

            // Set unread count
            if (room.getUnreadCount() > 0) {
                unreadBadge.setVisibility(View.VISIBLE);
                unreadBadge.setText(String.valueOf(room.getUnreadCount()));
            } else {
                unreadBadge.setVisibility(View.GONE);
            }
        }
    }
}