package com.miles.fitnessagent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private final List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        boolean user = "user".equals(message.role);
        holder.role.setText(user ? "You" : "Fitness Agent");
        holder.content.setText(message.content);
        holder.itemView.setBackgroundResource(user ? R.drawable.bg_message_user : R.drawable.bg_message_agent);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView role;
        TextView content;

        ViewHolder(View itemView) {
            super(itemView);
            role = itemView.findViewById(R.id.message_role);
            content = itemView.findViewById(R.id.message_content);
        }
    }
}
