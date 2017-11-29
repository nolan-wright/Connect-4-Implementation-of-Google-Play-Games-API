package com.purduegmail.mobileapps.project_v3;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";

    // request code constants
    private final static int RC_INVITATION_INBOX = 0;
    private final static int RC_ACHIEVEMENTS = 1;
    private final static int RC_SIGN_IN = 2;

    // name constants
    public static final String GAME_TYPE = "game-type";
    public static final String TYPE_AUTOMATCH = "auto-match";
    public static final String TYPE_PLAYER_SELECT = "player-select";
    public static final String TYPE_JOIN_INVITATION = "join-invitation";
    public static final String INVITATION_ID = "invitation";

    GoogleSignInClient signInClient;
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
        signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener( // attempt to sign in automatically
                new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    // silent sign in succeeded
                    Log.i(TAG, "Sign in succeeded");
                    GoogleSignInAccount account = task.getResult();
                    Games.getGamesClient(MainActivity.this, account)
                            .setViewForPopups(findViewById(android.R.id.content));
                    Games.getPlayersClient(MainActivity.this, account).getCurrentPlayer()
                            .addOnSuccessListener(new OnSuccessListener<Player>() {
                                @Override
                                public void onSuccess(Player player) {
                                    findViewById(R.id.signed_in_container)
                                            .setVisibility(View.VISIBLE);
                                    String signedInAs = getString(R.string.signed_in_as)
                                            + "\t" + player.getDisplayName();
                                    ((TextView)findViewById(R.id.signed_in_as)).setText(signedInAs);
                                }
                            });
                    invitationsClient =
                            Games.getInvitationsClient(MainActivity.this, account);
                    invitationsClient.registerInvitationCallback(new InvitationHandler());
                    goToInvitedGame(); // succeeds only if invite accepted from outside the app
                }
                else { // player will need to sign in manually
                    // silent sign in failed
                    Log.i(TAG, "Sign in failed");
                    findViewById(R.id.button_automatch).setEnabled(false);
                    findViewById(R.id.button_player_select).setEnabled(false);
                    findViewById(R.id.button_view_invitations).setEnabled(false);
                    findViewById(R.id.button_view_achievements).setEnabled(false);
                    findViewById(R.id.not_signed_in_container).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_google_sign_in).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = signInClient.getSignInIntent();
                            startActivityForResult(intent, RC_SIGN_IN);}
                    });
                }
            }
        });
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
        else if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account =
                        task.getResult(ApiException.class); // throws exception if sign in failed
                Log.i(TAG, "Manual sign in succeeded");
                Games.getGamesClient(this, account)
                        .setViewForPopups(findViewById(android.R.id.content));
                findViewById(R.id.button_automatch).setEnabled(true);
                findViewById(R.id.button_player_select).setEnabled(true);
                findViewById(R.id.button_view_invitations).setEnabled(true);
                findViewById(R.id.button_view_achievements).setEnabled(true);
                findViewById(R.id.not_signed_in_container).setVisibility(View.GONE);
            }
            catch (ApiException ex) {
                Log.i(TAG, "Manual sign in failed");
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*
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
    // view achievements button
    public void viewAchievements(View v) {
        AchievementsClient client = Games.getAchievementsClient(this,
                GoogleSignIn.getLastSignedInAccount(this));
        client.getAchievementsIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                startActivityForResult(intent, RC_ACHIEVEMENTS);
            }
        });
    }
    // play computer button
    public void playComputer(View v) {
        Intent intent = new Intent(this, PlayBotActivity.class);
        startActivity(intent);
    }
    // sign out button
    public void signOut(View v) {
        signInClient.signOut();
        findViewById(R.id.signed_in_container).setVisibility(View.GONE);
        this.onResume();
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