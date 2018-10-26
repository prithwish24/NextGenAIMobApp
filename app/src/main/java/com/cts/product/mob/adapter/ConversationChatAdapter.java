package com.cts.product.mob.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cts.product.mob.R;

import java.util.List;


/**
 * Adapter to display and recycle chat messages
 *
 * @author Prithwish
 */


public class ConversationChatAdapter extends RecyclerView.Adapter<ConversationChatAdapter.ViewHolder> {
    private List<ChatMessage> mMessages;

    public ConversationChatAdapter(List<ChatMessage> messages) {
        this.mMessages = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        if (viewType == ChatMessage.ChatDirection.Sent.ordinal()) {
            layout = R.layout.item_message_sent;
        } else if (viewType == ChatMessage.ChatDirection.Received.ordinal()) {
            layout = R.layout.item_message_received;
        }

        /*switch (viewType) {
            case ChatMessage.ChatDirection.Sent.ordinal():
                layout = R.layout.item_message_sent;
                break;
            case ChatMessage.VIEW_TYPE_MESSAGE_RECEIVED:
                layout = R.layout.item_message_received;
                break;
        }*/
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ChatMessage chatMessage = mMessages.get(position);
        holder.bind(chatMessage);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }


    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mChatText;
        //private TextView mTimeText;
        //private TextView mAuthor;

        ViewHolder(View itemView) {
            super(itemView);
            mChatText = itemView.findViewById(R.id.message_text);
            //mTimeText = itemView.findViewById(R.id.message_time);
            //mAuthor = itemView.findViewById(R.id.message_author);
        }

        void bind(ChatMessage chatMessage) {
            mChatText.setText(chatMessage.getMessage());
            /*mTimeText.setText(Utils.fmtTime(chatMessage.getCreatedAt()));
            if (chatMessage.getType() == ChatMessage.VIEW_TYPE_MESSAGE_RECEIVED) {
                mAuthor.setText(chatMessage.getSender());
            }*/
        }
    }
}
