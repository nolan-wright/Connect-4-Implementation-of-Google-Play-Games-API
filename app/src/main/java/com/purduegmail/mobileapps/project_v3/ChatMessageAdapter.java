package com.purduegmail.mobileapps.project_v3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Nolan Wright on 11/18/2017.
 */

public class ChatMessageAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 0;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 1;

    private LayoutInflater inflater;
    private List<ChatMessage> data;

    // constructor
    ChatMessageAdapter(Context context, List<ChatMessage> messages) {
        inflater = LayoutInflater.from(context);
        data = messages;
    }

    @Override
    public int getCount() {
        return (data == null) ? 0 : data.size();
    }

    @Override
    public ChatMessage getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    } // determines the # of possible views

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).wasSent()) {
            return VIEW_TYPE_MESSAGE_SENT;
        }
        else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    } // determines which view this item should have

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            if (getItemViewType(position) == VIEW_TYPE_MESSAGE_SENT) {
                view = inflater.inflate(R.layout.message_sent, viewGroup, false);
                SentMessageHolder holder = new SentMessageHolder(view);
                holder.bind(getItem(position));
                view.setTag(holder);
            }
            else { // was received
                view = inflater.inflate(R.layout.message_received, viewGroup, false);
                ReceivedMessageHolder holder = new ReceivedMessageHolder(view);
                holder.bind(getItem(position));
                view.setTag(holder);
            }
        }
        else {
            // simply update view contents
            ChatMessageViewHolder holder = (ChatMessageViewHolder)view.getTag();
            holder.bind(getItem(position));
        }
        return view;
    } // assigns view to item

    public void add(ChatMessage message) {
        data.add(message);
        notifyDataSetChanged();
    }

    /*
     * nested classes
     */
    private interface ChatMessageViewHolder {
        void bind(ChatMessage message);
    }
    private class SentMessageHolder implements ChatMessageViewHolder {

        private WrapWidthTextView messageView;

        // constructor
        SentMessageHolder(View itemView) {
            messageView = itemView.findViewById(R.id.messageSent);
        }

        public void bind(ChatMessage message) {
            messageView.setText(message.getMessage());
        }

    }
    private class ReceivedMessageHolder implements ChatMessageViewHolder {

        private WrapWidthTextView messageView;

        // constructor
        ReceivedMessageHolder(View itemView) {
            messageView = itemView.findViewById(R.id.messageReceived);
        }

        public void bind(ChatMessage message) {
            messageView.setText(message.getMessage());
        }

    }

}