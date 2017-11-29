package com.purduegmail.mobileapps.project_v3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    final static String TAG = "ChatActivity";

    // name constants
    public final static String MESSAGE_SENT = "message-sent";
    public final static String MESSAGE_SENT_EVENT = "message-sent-event";
    public final static int INVITATION_ACCEPTED = 4444;

    private ChatMessageAdapter adapter;
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(GameActivity.MESSAGE_RECEIVED);
            // add message to chat
            adapter.add(new ChatMessage(message, false));
        }
    };
    private boolean gameIsOngoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set up UI
        displayChatActionBar();
        setContentView(R.layout.chat);
        // register broadcastreceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
                new IntentFilter(GameActivity.MESSAGE_RECEIVED_EVENT));
        // determine whether game is ongoing
        gameIsOngoing = getIntent().getBooleanExtra(GameActivity.GAME_STATUS, false);
        // get messages from intent
        ArrayList<ChatMessage> messages =
                getIntent().getParcelableArrayListExtra(GameActivity.MESSAGES);
        // add messages to chat
        adapter = new ChatMessageAdapter(this, messages);
        ListView chatView = findViewById(R.id.chatView);
        chatView.setAdapter(adapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // get user's sign in account
        InvitationsClient client = Games.getInvitationsClient(this,
                GoogleSignIn.getLastSignedInAccount(this));
        client.registerInvitationCallback(new ChatActivity.InvitationHandler());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }

    /*
     * on click methods
     */
    // exit button
    public void exitChat(View v) {
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

    /*
     * nested classes
     */
    class InvitationHandler extends InvitationCallback {
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            Log.i(TAG, "Invitation received");
            showInvitationSnackbar(invitation);
        }
        @Override
        public void onInvitationRemoved(@NonNull String s) {
            Log.i(TAG, "Invitation removed");
        }
    }

    /*
     * helper methods
     */
    // called in InvitationHandler.onInvitationReceived
    private void showInvitationSnackbar(final Invitation invitation) {
        View content = findViewById(android.R.id.content);
        String snackbar_message = invitation.getInviter().getDisplayName() + " "
                + getResources().getString(R.string.snackbar_text);
        Snackbar.make(content, snackbar_message, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_action_text, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // prepare intent
                        if (!gameIsOngoing) {
                            Intent intent = new Intent(ChatActivity.this, GameActivity.class);
                            intent.putExtra(MainActivity.GAME_TYPE, MainActivity.TYPE_JOIN_INVITATION);
                            intent.putExtra(MainActivity.INVITATION_ID, invitation.getInvitationId());
                            startActivity(intent); // start new activity
                        }
                        else {
                            Intent intent = new Intent();
                            intent.putExtra(MainActivity.INVITATION_ID, invitation.getInvitationId());
                            setResult(INVITATION_ACCEPTED, intent);
                            finish();
                        }
                    }
                })
                .show();
    }
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