package com.purduegmail.mobileapps.project_v3;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";

    // request code constants
    final static int RC_INVITATION_INBOX = 0;

    // name constants
    public static final String GAME_TYPE = "gameType";
    public static final String TYPE_AUTOMATCH = "auto-match";
    public static final String TYPE_PLAYER_SELECT = "player-select";
    public static final String TYPE_JOIN_INVITATION = "join-invitation";
    public static final String INVITATION_ID = "invitation";

    InvitationsClient invitationsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // get user's sign in account
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn();
        invitationsClient = Games.getInvitationsClient(this,
                GoogleSignIn.getLastSignedInAccount(this));
        invitationsClient.registerInvitationCallback(new InvitationHandler());
        goToInvitedGame(); // succeeds only if invite accepted from outside the app
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_INVITATION_INBOX) {
            if (resultCode != RESULT_OK) {
                return;
            }
            Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
            if (invitation != null) {
                // prepare intent
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra(MainActivity.GAME_TYPE, MainActivity.TYPE_JOIN_INVITATION);
                intent.putExtra(MainActivity.INVITATION_ID, invitation.getInvitationId());
                startActivity(intent); // start new activity
            }
        }
    }

    /**
     * onClick methods
     */
    // new game button
    public void automatch(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GAME_TYPE, TYPE_AUTOMATCH);
        startActivity(intent);
    }
    // invite friend button
    public void playerSelect(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GAME_TYPE, TYPE_PLAYER_SELECT);
        startActivity(intent);
    }
    // view invitations button
    public void viewInvitations(View v) {
        invitationsClient.getInvitationInboxIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_INVITATION_INBOX);
                    }
                });
    }

    /**
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

    /**
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
                        Intent intent = new Intent(MainActivity.this, GameActivity.class);
                        intent.putExtra(MainActivity.GAME_TYPE, MainActivity.TYPE_JOIN_INVITATION);
                        intent.putExtra(MainActivity.INVITATION_ID, invitation.getInvitationId());
                        startActivity(intent); // start new activity
                    }
                })
                .show();
    }
    // called in onResume
    private void goToInvitedGame() {
        Games.getGamesClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getActivationHint()
                .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                    @Override
                    public void onSuccess(Bundle bundle) {
                        if (bundle == null) {
                            return;
                        }
                        Invitation invitation = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);
                        if (invitation != null) {
                            // app was started from accepting invitation notification
                            Intent intent = new Intent(MainActivity.this, GameActivity.class);
                            intent.putExtra(GAME_TYPE, TYPE_JOIN_INVITATION);
                            intent.putExtra(INVITATION_ID, invitation.getInvitationId());
                            startActivity(intent);
                        }

                    }
                });
    }

}