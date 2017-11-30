package com.purduegmail.mobileapps.project_v3;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PlayBotActivity extends AppCompatActivity
    implements GameFragment.GameFragmentListener, Game.GameListener {

    final String TAG = "PlayBotActivity";

    // implementation of GameFragmentListener
    public void onColumnClicked(int column) {
        game.processMyMove(column);
    }
    public void onFragmentInflated() {
        if (botGoesFirst) {
            fragment.setColumnsClickable(false);
            showSpinner();
            new BotThread().execute();
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
        }
        else if (hasTied) {
            showInformationalDialog(getResources().getString(R.string.dialog_message_tie),
                    getResources().getString(R.string.dialog_title_completed));
        }
        else {
            showSpinner();
            new BotThread().execute();
        }
    }
    public void onGameTied() {
        hasTied = true;
    }
    public void onGameWon(ArrayList<int[][]> winningSequences) {
        hasWon = true;
        this.winningSequences = winningSequences;
    }

    private Game game;
    private GameFragment fragment;
    private GameBot bot;
    private boolean botGoesFirst = false;
    private ArrayList<int[][]> winningSequences;
    private boolean hasWon = false;
    private boolean hasTied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.difficulty_selection);
        displayCustomActionBar();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // get user's sign in account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) { // player is signed in
            InvitationsClient client = Games.getInvitationsClient(this, account);
            client.registerInvitationCallback(new PlayBotActivity.InvitationHandler());
        }
    }

    /*
     * on click methods
     */
    public void play(View v) {
        int bot_difficulty;
        SeekBar difficulty_selector = findViewById(R.id.bot_difficulty);
        if (difficulty_selector.getProgress() == 0) {
            bot_difficulty = 2;
        }
        else if (difficulty_selector.getProgress() == 1) {
            bot_difficulty = 4;
        }
        else {
            bot_difficulty = 6;
        }
        bot = new GameBot(bot_difficulty);
        botGoesFirst = ((Switch)findViewById(R.id.switch_who_goes_first)).isChecked();
        initializeGameplayUI();
    }
    public void exit(View v) {
        finish();
    }

    /*
     * nested class
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
    private class BotThread extends AsyncTask<Void, Void, int[]> {
        @Override
        protected int[] doInBackground(Void... arg0) {
            return bot.move(game.getBoard());
        }
        @Override
        protected void onPostExecute(int[] botMove) {
            makeBotMove(botMove);
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
                        Intent intent = new Intent(PlayBotActivity.this, GameActivity.class);
                        intent.putExtra(MainActivity.GAME_TYPE, MainActivity.TYPE_JOIN_INVITATION);
                        intent.putExtra(MainActivity.INVITATION_ID, invitation.getInvitationId());
                        startActivity(intent); // start new activity
                    }
                })
                .show();
    }
    // called in onMyMoveProcessed
    private void showSpinner() {
        findViewById(R.id.progressBar_Player2).setVisibility(View.VISIBLE);
    }
    // called in makeBotMove
    private void dismissSpinner() {
        findViewById(R.id.progressBar_Player2).setVisibility(View.GONE);
    }
    // called in play
    private void initializeGameplayUI() {
        setContentView(R.layout.activity_game);
        // set pictures
        ImageView iv_computerPicture = findViewById(R.id.profilePic_Player2);
        ImageView iv_humanPicture = findViewById(R.id.profilePic_Player1);
        iv_computerPicture.setImageResource(R.drawable.android);
        iv_humanPicture.setImageResource(R.drawable.human);
        // set player labels
        TextView tv_computerName = findViewById(R.id.displayName_Player2);
        TextView tv_humanName = findViewById(R.id.displayName_Player1);
        tv_computerName.setText(R.string.player2_label_computer);
        tv_humanName.setText(R.string.player1_label_player);
        // set fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = GameFragment.newInstance(this);
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
        // initialize game
        game = new Game(this);
        winningSequences = new ArrayList<>();
    }
    // called in onCreate
    // called in initializeGameplayUI
    private void displayCustomActionBar() {
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        LayoutInflater li = LayoutInflater.from(this);
        View custom_actionbar_view = li.inflate(R.layout.play_computer_actionbar, null);
        ab.setCustomView(custom_actionbar_view);
        ab.setDisplayShowCustomEnabled(true);
    }
    // called in BotThread.onPostExecute
    private void makeBotMove(int[] botMove) {
        dismissSpinner();
        fragment.drawUpdate(botMove[0], botMove[1], Game.OPPONENT_MARKER);
        game.processOpponentMove(botMove[0], botMove[1]);
        ArrayList<int[][]> losingSequences = game.getLosingSequences();
        if (!losingSequences.isEmpty()) {
            fragment.highlightSequences(losingSequences, Game.OPPONENT_MARKER);
            showInformationalDialog(getResources().getString(R.string.dialog_message_lost),
                    getResources().getString(R.string.dialog_title_completed));
        }
        else {
            fragment.setColumnsClickable(true);
        }
    }
    // called in makeBotMove
    // called in onMyMoveProcessed
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

}