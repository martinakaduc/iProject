package com.ducnguyen.iproject.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ducnguyen.iproject.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final String TAG = "MessageAdapter";
    private final List<Message> mMessages;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    public MessageAdapter(Context context, List<Message> data) {
        mContext = context;
        mMessages = data;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.cardview_row, parent, false);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = mMessages.get(position);

        holder.setTopicName(message.getTopic());
        holder.setTimeView((message.getTime()));
        holder.setMessageView(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView topicName;
        private final TextView timeView;
        private final TextView messageView;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            topicName = itemView.findViewById(R.id.topicName);
            timeView = itemView.findViewById(R.id.timeView);
            messageView = itemView.findViewById(R.id.messageView);
        }

        public void setTimeView(String time) {
            timeView.setText(time);
        }

        public void setTopicName(String topic) {
           topicName.setText(topic);
        }

        public void setMessageView(String message ) {
            messageView.setText(message);
        }
    }
}
