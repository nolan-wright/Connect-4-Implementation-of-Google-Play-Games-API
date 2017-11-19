package com.purduegmail.mobileapps.project_v3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    // name constants
    public final static String MESSAGE_SENT = "message-sent";
    public final static String MESSAGE_SENT_EVENT = "message-sent-event";

    private ChatMessageAdapter adapter;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(GameActivity.MESSAGE_RECEIVED);
            // add message to chat
            adapter.add(new ChatMessage(message, false));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set up UI
        displayChatActionBar();
        setContentView(R.layout.chat);
        // register broadcastreceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
                new IntentFilter(GameActivity.MESSAGE_RECEIVED_EVENT));
        // get messages from intent
        ArrayList<ChatMessage> messages =
                getIntent().getParcelableArrayListExtra(GameActivity.MESSAGES);
        // add messages to chat
        adapter = new ChatMessageAdapter(this, messages);
        ListView chatView = findViewById(R.id.chatView);
        chatView.setAdapter(adapter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }

    /**
     * on click methods
     */
    // exit button
    public void exit(View v) {
        finish();
    }
    // send button
    public void send(View v) {
        EditText et = findViewById(R.id.et_composeMessage);
        String message = et.getText().toString();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        et.setText("");
        // add message to chat
        adapter.add(new ChatMessage(message, true));
        broadcastMessage(message);
    }

    /**
     * helper methods
     */
    // called in send
    private void broadcastMessage(String message) {
        Intent intent = new Intent(MESSAGE_SENT_EVENT);
        intent.putExtra(MESSAGE_SENT, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    // called in onCreate
    private void displayChatActionBar() {
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        LayoutInflater li = LayoutInflater.from(this);
        View custom_actionbar_view = li.inflate(R.layout.chat_actionbar, null);
        ab.setCustomView(custom_actionbar_view);
        ab.setDisplayShowCustomEnabled(true);
    }
}