package com.miles.fitnessagent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    public interface OnConversationClick {
        void onClick(Conversation conversation);
    }

    private final List<Conversation> conversations;
    private final OnConversationClick listener;

    public ConversationAdapter(List<Conversation> conversations, OnConversationClick listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.title.setText(conversation.title);
        holder.subtitle.setText("Updated " + conversation.updatedAt);
        holder.itemView.setOnClickListener(v -> listener.onClick(conversation));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.conversation_title);
            subtitle = itemView.findViewById(R.id.conversation_subtitle);
        }
    }
}
