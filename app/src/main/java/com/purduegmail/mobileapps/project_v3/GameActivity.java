package com.purduegmail.mobileapps.project_v3;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameActivity extends AppCompatActivity
    implements GameFragment.GameFragmentListener, Game.GameListener {

    final static String TAG = "GameActivity";

    // request code constants
    private final static int RC_SELECT_PLAYERS = 0;
    private final static int RC_WAITING_FOR_PLAYERS = 1;
    private final static int RC_CHAT = 2;

    // name constants
    public final static String MESSAGES = "chat-history";
    public final static String MESSAGE_RECEIVED = "message-received";
    public final static String MESSAGE_RECEIVED_EVENT = "message-received-event";
    public final static String GAME_STATUS = "game-status";

    // status constants
    private final static int MATCH_ACTIVE = 0;
    private final static int MATCH_DECIDED = 1;
    private final static int MATCH_DRAWN = 2;
    private final static int CHAT_MESSAGE = 3;

    // implementation of GameFragmentListener
    public void onColumnClicked(int column) {
        game.processMyMove(column);
    }
    public void onFragmentInflated() {
        // set turn order
        if (!goesFirst) {
            fragment.setColumnsClickable(false);
            showSpinner();
        }
    }

    // implementation of GameListener
    public void onMyMoveProcessed(int row, int column) {
        fragment.setColumnsClickable(false);
        fragment.drawUpdate(row, column, Game.MY_MARKER);
        if (hasWon) {
            fragment.highlightSequences(winningSequences, Game.MY_MARKER);
            showInformationalDialog(getResources().getString(R.string.dialog_message_won),
                    getResources().getString(R.string.dialog_title_completed));
            // send message to opponent
            byte[] data = {(byte)MATCH_DECIDED, (byte)row, (byte)column};
            client.sendReliableMessage(data, room.getRoomId(), opponentParticipantId,
                    new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int i, int i1, String s) {
                            Log.i(TAG, "Message sent, match won");
                        }
                    });
            // leaderboard logic
            updatePlayerWins();
            // achievements logic
            checkForAchievements();
        }
        else if (hasTied) {
            showInformationalDialog(getResources().getString(R.string.dialog_message_tie),
                    getResources().getString(R.string.dialog_title_completed));
            byte[] data = {(byte)MATCH_DRAWN, (byte)row, (byte)column};
            client.sendReliableMessage(data, room.getRoomId(), opponentParticipantId,
                    new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int i, int i1, String s) {
                            Log.i(TAG, "Message sent, match tied");
                        }
                    });
        }
        else {
            byte[] data = {(byte)MATCH_ACTIVE, (byte)row, (byte)column};
            client.sendReliableMessage(data, room.getRoomId(), opponentParticipantId,
                    new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int i, int i1, String s) {
                            Log.i(TAG, "Message sent");
                        }
                    });
            showSpinner();
        }
    }
    public void onGameTied() {
        hasTied = true;
        gameIsOngoing = false;
    }
    public void onGameWon(ArrayList<int[][]> winningSequences) {
        hasWon = true;
        gameIsOngoing = false;
        this.winningSequences = winningSequences;
    }

    private RealTimeMultiplayerClient client; // talks to google play services
    private RoomConfig config; // describes the room, so that it can be created/joined
    private String myPlayerId; // "primary key" for "users" reference in database
    private String myParticipantId;
    private String opponentParticipantId;
    private boolean goesFirst;
    private Room room; // provides connectivity between participants
    private Game game; // connect 4 game logic
    private GameFragment fragment; // game display... the big, blue square with white holes
    private ArrayList<int[][]> winningSequences;
    private boolean hasWon = false;
    private boolean hasTied = false;
    private boolean gameIsOngoing = false;
    private ArrayList<ChatMessage> messages;
    private BroadcastReceiver messageReceiver; // receives sent messages from ChatActivity
    private boolean isInFocus = false; // whether this activity has the focus / is being displayed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        // initialize clients
        client = Games.getRealTimeMultiplayerClient(this,
                GoogleSignIn.getLastSignedInAccount(this));
        InvitationsClient invitationsClient = Games.getInvitationsClient(this,
                GoogleSignIn.getLastSignedInAccount(this));
        invitationsClient.registerInvitationCallback(new InvitationHandler());
        // get game type
        String type = getIntent().getStringExtra(MainActivity.GAME_TYPE);
        switch (type) {
            case MainActivity.TYPE_AUTOMATCH:
                // auto-match game requested
                Log.i(TAG, "Auto-match game requested");
                createAutomatchGame();
                break;
            case MainActivity.TYPE_PLAYER_SELECT:
                // invite player game requested
                Log.i(TAG, "Player-select game requested");
                createInviteGame();
                break;
            case MainActivity.TYPE_JOIN_INVITATION:
                // joining game to which player was invited
                Log.i(TAG, "Joining invitation");
                joinInvitedGame();
                break;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        isInFocus = true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        isInFocus = false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                if (resultCode != RESULT_OK) {
                    finish();
                    return;
                }
                Log.i(TAG, "Opponent selected");
                // create game with invited player
                String invitee = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS).get(0);
                config = RoomConfig.builder(new RoomUpdateHandler())
                        .setOnMessageReceivedListener(new MessageReceivedHandler())
                        .setRoomStatusUpdateCallback(new RoomStatusUpdateHandler())
                        .addPlayersToInvite(invitee)
                        .build();
                client.create(config);
                break;
            case RC_WAITING_FOR_PLAYERS:
                if (resultCode != RESULT_OK) {
                    client.leave(config, room.getRoomId()); // leave room
                    finish(); // close activity
                    return;
                }
                Log.i(TAG, "Done waiting for players");
                break;
            case RC_CHAT:
                if (resultCode == ChatActivity.INVITATION_ACCEPTED) {
                    // go to invitation
                    String invitationId = data.getStringExtra(MainActivity.INVITATION_ID);
                    if (gameIsOngoing) {
                        showConfirmForfeitureDialog(invitationId);
                    }
                    else {
                        client.leave(config, room.getRoomId()); // leave the room
                        // prepare intent
                        Intent intent = new Intent(GameActivity.this, GameActivity.class);
                        intent.putExtra(MainActivity.GAME_TYPE, MainActivity.TYPE_JOIN_INVITATION);
                        intent.putExtra(MainActivity.INVITATION_ID, invitationId);
                        finish(); // close this activity
                        startActivity(intent); // start new activity
                    }
                }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        if (room != null) {
            client.leave(config, room.getRoomId()); // leave room
        }
    }

    /*
     * nested classes
     */
    // used in RoomConfig builder
    class RoomUpdateHandler extends RoomUpdateCallback {
        @Override
        public void onRoomCreated(int statusCode, Room r) {
            Log.i(TAG, "Room created");
            room = r;
            showPlayerWaitingUI(room);
        }
        @Override
        public void onLeftRoom(int statusCode, String roomId) {
            Log.i(TAG, "Left room");
        }
        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            Log.i(TAG, "Joined room");
            if (room == null) {
                showFailureToJoinDialog();
            }
        }
        @Override
        public void onRoomConnected(int statusCode, Room r) {
            Log.i(TAG, "Room connected");
            room = r;
            initializeGame();
        }
    }
    class MessageReceivedHandler implements OnRealTimeMessageReceivedListener {
        public void onRealTimeMessageReceived(RealTimeMessage message) {
            byte[] data = message.getMessageData();
            int status = (int)data[0];
            int row, column;
            switch (status) {
                case MATCH_ACTIVE:
                    dismissSpinner();
                    row = (int)data[1];
                    column = (int)data[2];
                    game.processOpponentMove(row, column);
                    fragment.drawUpdate(row, column, Game.OPPONENT_MARKER);
                    fragment.setColumnsClickable(true);
                    break;
                case MATCH_DECIDED: // match lost
                    dismissSpinner();
                    gameIsOngoing = false;
                    row = (int)data[1];
                    column = (int)data[2];
                    game.processOpponentMove(row, column);
                    fragment.drawUpdate(row, column, Game.OPPONENT_MARKER);
                    fragment.highlightSequences(game.getLosingSequences(), Game.OPPONENT_MARKER);
                    showInformationalDialog(getResources().getString(R.string.dialog_message_lost),
                            getResources().getString(R.string.dialog_title_completed));
                    break;
                case MATCH_DRAWN:
                    dismissSpinner();
                    gameIsOngoing = false;
                    row = (int)data[1];
                    column = (int)data[2];
                    game.processOpponentMove(row, column);
                    fragment.drawUpdate(row, column, Game.OPPONENT_MARKER);
                    showInformationalDialog(getResources().getString(R.string.dialog_message_tie),
                            getResources().getString(R.string.dialog_title_completed));
                    break;
                case CHAT_MESSAGE: // opponent sent a chat message
                    byte[] chatMessageInBytes = Arrays.copyOfRange(data, 1, data.length);
                    String chatMessage = new String(chatMessageInBytes);
                    broadcastMessage(chatMessage);
                    messages.add(new ChatMessage(chatMessage, false));
                    if (isInFocus) {
                        showChatNotification();
                    }
                    break;
            }
        }
    }
    class RoomStatusUpdateHandler extends RoomStatusUpdateCallback {
        @Override
        public void onRoomConnecting(@Nullable Room room) {
            Log.i(TAG, "Room connecting");
        }
        @Override
        public void onRoomAutoMatching(@Nullable Room room) {
            Log.i(TAG, "Room auto-matching");
        }
        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {
            Log.i(TAG, "Peer invited to room");
        }
        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {
            Log.i(TAG, "Peer declined");
        }
        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {
            Log.i(TAG, "Peer joined");
        }
        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
            Log.i(TAG, "Peer left");
            if (gameIsOngoing) {
                // opponent forfeited
                gameIsOngoing = false;
                dismissSpinner();
                showInformationalDialog(getResources().getString(R.string.dialog_message_won),
                        getResources().getString(R.string.dialog_title_forfeited));
            }
        }
        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            Log.i(TAG, "Connected to room");
        }
        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {
            Log.i(TAG, "Disconnected from room");
        }
        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            Log.i(TAG, "Peers connected");
        }
        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
            Log.i(TAG, "Peers disconnected");
        }
        @Override
        public void onP2PConnected(@NonNull String s) {
            Log.i(TAG, "P2P connected");
        }
        @Override
        public void onP2PDisconnected(@NonNull String s) {
            Log.i(TAG, "P2P disconnected");
        }
    }
    // used in onCreate
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
     * onClick methods
     */
    // chat button
    public void chat(View v) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(MESSAGES, messages);
        intent.putExtra(GAME_STATUS, gameIsOngoing);
        dismissChatNotification();
        startActivityForResult(intent, RC_CHAT);
    }
    // exit button
    public void exit(View v) {
        if (!gameIsOngoing) {
            client.leave(config, room.getRoomId()); // leave room
            finish(); // close activity
            return;
        }
        // game is in progress
        showConfirmForfeitureDialog();
    }

    /*
     * helper methods
     */
    // called in updatePlayerWins
    private void updateLeaderboard(int wins) {
        LeaderboardsClient client = Games.getLeaderboardsClient(this,
                GoogleSignIn.getLastSignedInAccount(this));
        client.submitScore(getString(R.string.leaderboard_most_wins), wins + 1);
    }
    // called in onMyMoveProcessed
    private void updatePlayerWins() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("users").child(myPlayerId).child("wins")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer wins = dataSnapshot.getValue(Integer.class);
                if (wins == null) { // this is the player's first win
                    wins = 0;
                }
                updateLeaderboard(wins);
                ref.child("users").child(myPlayerId).child("wins")
                        .setValue(wins + 1);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "Error reading from database", databaseError.toException());
            }
        });
    }
    // called in onMyMoveProcessed
    private void checkForAchievements() {
        Games.getGamesClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .setViewForPopups(findViewById(android.R.id.content))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        AchievementsClient client = Games.getAchievementsClient(GameActivity.this,
                                GoogleSignIn.getLastSignedInAccount(GameActivity.this));
                        if (winningSequences.size() > 1) {
                            client.unlock(getString(R.string.achievement_master_tiger));
                        }
                        for (int[][] sequence : winningSequences) {
                            if (sequence.length > 4) {
                                client.unlock(getString(R.string.achievement_expert_lion));
                                return;
                            }
                        }}
                });
    }
    // called in MessageReceivedHandler.onRealTimeMessageReceived
    private void showChatNotification() {
        ((ImageView)findViewById(R.id.ib_chat)).setImageResource(R.drawable.chat_messages_received);
    }
    // called in chat
    private void dismissChatNotification() {
        ((ImageView)findViewById(R.id.ib_chat)).setImageResource(R.drawable.chat);
    }
    // called in MessageReceivedHandler.onRealTimeMessageReceived
    private void broadcastMessage(String message) {
        Intent intent = new Intent(MESSAGE_RECEIVED_EVENT);
        intent.putExtra(MESSAGE_RECEIVED, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    // called in initializeUI
    private void displayCustomActionBar() {
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        LayoutInflater li = LayoutInflater.from(this);
        View custom_actionbar_view = li.inflate(R.layout.custom_actionbar, null);
        ab.setCustomView(custom_actionbar_view);
        ab.setDisplayShowCustomEnabled(true);
    }
    // called in InvitationHandler.onInvitationReceived
    private void showInvitationSnackbar(final Invitation invitation) {
        View content = findViewById(android.R.id.content);
        String snackbar_message = invitation.getInviter().getDisplayName() + " "
                + getResources().getString(R.string.snackbar_text);
        Snackbar.make(content, snackbar_message, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_action_text, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (gameIsOngoing) {
                            showConfirmForfeitureDialog(invitation.getInvitationId());
                        }
                        else {
                            client.leave(config, room.getRoomId()); // leave the room
                            // prepare intent
                            Intent intent = new Intent(GameActivity.this, GameActivity.class);
                            intent.putExtra(MainActivity.GAME_TYPE, MainActivity.TYPE_JOIN_INVITATION);
                            intent.putExtra(MainActivity.INVITATION_ID, invitation.getInvitationId());
                            finish(); // close this activity
                            startActivity(intent); // start new activity
                        }
                    }
                })
                .show();
    }
    // called in RoomUpdateHandler.onRoomJoined
    private void showFailureToJoinDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(getResources().getString(R.string.dialog_title_join_failure));
        dialog.setMessage(getResources().getString(R.string.dialog_message_join_failure));
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.dialog_neutral_button_text),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish(); // close the activity
                    }
                });
        dialog.show();
    }
    // called in showInvitationSnackbar
    private void showConfirmForfeitureDialog(final String invitationId) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(getResources().getString(R.string.dialog_title_confirmation));
        dialog.setMessage(getResources().getString(R.string.dialog_message_forfeit));
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.dialog_negative_button_text),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.dialog_positive_button_text),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        client.leave(config, room.getRoomId()); // leave the room
                        // prepare intent
                        Intent intent = new Intent(GameActivity.this, GameActivity.class);
                        intent.putExtra(MainActivity.GAME_TYPE, MainActivity.TYPE_JOIN_INVITATION);
                        intent.putExtra(MainActivity.INVITATION_ID, invitationId);
                        finish(); // close this activity
                        startActivity(intent); // start new activity
                    }
                });
        dialog.show();
    }
    // called in exit
    private void showConfirmForfeitureDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(getResources().getString(R.string.dialog_title_confirmation));
        dialog.setMessage(getResources().getString(R.string.dialog_message_forfeit));
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.dialog_negative_button_text),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.dialog_positive_button_text),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        client.leave(config, room.getRoomId()); // leave the room
                        finish(); // close activity
                    }
                });
        dialog.show();
    }
    // called in GameListener.onMyMoveProcessed
    // called in MessageReceivedHandler.onRealTimeMessageReceived
    private void showInformationalDialog(String message, String title) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.dialog_neutral_button_text),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        dialog.show();
    }
    // called in GameFragmentListener.onFragmentInflated
    // called in GameListener.onMyMoveProcessed
    private void showSpinner() {
        findViewById(R.id.progressBar_Player2).setVisibility(View.VISIBLE);
    }
    // called in RoomStatusUpdateHandler.onPeerLeft
    // called in MessageReceivedHandler.onRealTimeMessageReceived
    private void dismissSpinner() {
        findViewById(R.id.progressBar_Player2).setVisibility(View.GONE);
    }
    // called in RoomUpdateHandler.onRoomConnected
    private void initializeGame() {
        Games.getPlayersClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getCurrentPlayerId().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String playerId) {
                myPlayerId = playerId;
                // establish participant ids
                myParticipantId = room.getParticipantId(playerId);
                int index = room.getParticipantIds().indexOf(myParticipantId);
                if (index == 0) {
                    goesFirst = true;
                    opponentParticipantId = room.getParticipantIds().get(1);
                }
                else {
                    goesFirst = false;
                    opponentParticipantId = room.getParticipantIds().get(0);
                }
                game = new Game(GameActivity.this); // initialize game
                initializeUI();
                messages = new ArrayList<>();
                // initialize broadcastreceiver
                messageReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String message = intent.getStringExtra(ChatActivity.MESSAGE_SENT);
                        // prepare communication to opponent
                        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                        byte[] data = new byte[messageBytes.length + 1];
                        data[0] = (byte)CHAT_MESSAGE;
                        System.arraycopy(messageBytes, 0, data, 1, messageBytes.length);
                        // send communication
                        client.sendReliableMessage(data, room.getRoomId(), opponentParticipantId,
                                new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                                    @Override
                                    public void onRealTimeMessageSent(int i, int i1, String s) {
                                        Log.i(TAG, "Message sent, chat");
                                    }
                                });
                        messages.add(new ChatMessage(message, true));
                    }
                };
                // register broadcastreceiver
                LocalBroadcastManager.getInstance(GameActivity.this).registerReceiver(messageReceiver,
                        new IntentFilter(ChatActivity.MESSAGE_SENT_EVENT));
                gameIsOngoing = true; // game has begun
            }
        });
    }
    // called in initializeGame
    private void initializeUI() {
        displayCustomActionBar();
        setContentView(R.layout.activity_game);
        // initialize player displays
        TextView displayName_player1 = findViewById(R.id.displayName_Player1);
        TextView displayName_player2 = findViewById(R.id.displayName_Player2);
        ImageView profilePic_player1 = findViewById(R.id.profilePic_Player1);
        ImageView profilePic_player2 = findViewById(R.id.profilePic_Player2);
        // display participant information
        displayName_player1.setText(room.getParticipant(myParticipantId).getDisplayName());
        displayName_player2.setText(room.getParticipant(opponentParticipantId).getDisplayName());
        ImageManager manager = ImageManager.create(this);
        manager.loadImage(profilePic_player1, room.getParticipant(myParticipantId).getIconImageUri());
        manager.loadImage(profilePic_player2, room.getParticipant(opponentParticipantId).getIconImageUri());
        // set up fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = GameFragment.newInstance(this);
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
    // called in onCreate
    private void createAutomatchGame() {
        // create auto-match game
        final int MIN_OTHER_PLAYERS = 1;
        final int MAX_OTHER_PLAYERS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OTHER_PLAYERS, MAX_OTHER_PLAYERS, 0);
        // build room config
        config = RoomConfig.builder(new RoomUpdateHandler())
                .setOnMessageReceivedListener(new MessageReceivedHandler())
                .setRoomStatusUpdateCallback(new RoomStatusUpdateHandler())
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        // create room
        Log.i(TAG, "Creating room");
        client.create(config);
    }
    private void createInviteGame() {
        final int minPlayers = 1; // The minimum number of players to invite (not including the current player)
        final int maxPlayers = 1; // The maximum number of players to invite (not including the current player)
        final boolean allowAutomatch = false;
        client.getSelectOpponentsIntent(minPlayers, maxPlayers, allowAutomatch)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_SELECT_PLAYERS);
                    }
                });
    }
    private void joinInvitedGame() {
        String invitationId = getIntent().getStringExtra(MainActivity.INVITATION_ID);
        config = RoomConfig.builder(new RoomUpdateHandler())
                .setOnMessageReceivedListener(new MessageReceivedHandler())
                .setRoomStatusUpdateCallback(new RoomStatusUpdateHandler())
                .setInvitationIdToAccept(invitationId)
                .build();
        // join room
        client.join(config);
    }
    // called in RoomUpdateHandler.onRoomCreated
    private void showPlayerWaitingUI(Room room) {
        final int MIN_PLAYERS_TO_START = 2;
        client.getWaitingRoomIntent(room, MIN_PLAYERS_TO_START).addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                startActivityForResult(intent, RC_WAITING_FOR_PLAYERS);
            }
        });
    }

}