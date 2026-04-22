package com.miles.fitnessagent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    public interface OnConversationClick {
        void onClick(Conversation conversation);
    }

    public interface OnConversationAction {
        void onAction(Conversation conversation, View anchor);
    }

    private final List<Conversation> conversations;
    private final OnConversationClick listener;
    private final OnConversationAction actionListener;

    public ConversationAdapter(
            List<Conversation> conversations,
            OnConversationClick listener,
            OnConversationAction actionListener
    ) {
        this.conversations = conversations;
        this.listener = listener;
        this.actionListener = actionListener;
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
        holder.actionButton.setOnClickListener(v -> actionListener.onAction(conversation, v));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;
        ImageButton actionButton;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.conversation_title);
            subtitle = itemView.findViewById(R.id.conversation_subtitle);
            actionButton = itemView.findViewById(R.id.conversation_action_button);
        }
    }
}
