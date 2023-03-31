package com.gameditors.TheOrginal2048;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.EventBuffer;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity
{
    public static int mRewardDeletes = 2;

    // delete selection:
    public static int mRewardDeletingSelectionAmounts = 3;

    private static final String REWARD_DELETES = "reward chances";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String SCORE = "score";
    private static final String HIGH_SCORE = "high score temp";
    private static final String UNDO_SCORE = "undo score";
    private static final String CAN_UNDO = "can undo";
    private static final String UNDO_GRID = "undo";
    private static final String GAME_STATE = "game state";
    private static final String UNDO_GAME_STATE = "undo game state";
    private static final String REWARD_DELETE_SELECTION = "reward delete selection amounts";

    private MainView view;

    // Google Play Games Services:
    public GoogleSignInClient mGoogleSignInClient;  // Client used to sign in with Google APIs

    // Client variables
    public AchievementsClient mAchievementsClient;
    public LeaderboardsClient mLeaderboardsClient;
    public EventsClient mEventsClient;
    public PlayersClient mPlayersClient;

    // request codes we use when invoking an external activity
    public static final int RC_SIGN_IN = 9001;

    // tag for debug logging
    public final String TAG = "TanC";

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    private static long mHighScore4x4;
    private static long mHighScore5x5;
    private static long mHighScore6x6;
    private static long mHighScore8x8;
    private static long mHighScore11x11;
    private static long mHighScore15x15;
    private static long mHighScore25x25;
    private static long mHighScore50x50;
    private static long mHighScore100x100;

    private static boolean mAchievement32 = false;
    private static boolean mAchievement64 = false;
    private static boolean mAchievement128 = false;
    private static boolean mAchievement256 = false;
    private static boolean mAchievement512 = false;
    private static boolean mAchievement1024 = false;
    private static boolean mAchievement2048 = false;
    private static boolean mAchievement4096 = false;
    private static boolean mAchievement8192 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Context context = getApplicationContext();

        final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // Network is available
                MobileAds.initialize(context, initializationStatus -> {});
            }
        };

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        FrameLayout frameLayout = findViewById(R.id.game_frame_layout);
        view = new MainView(this, this);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        view.hasSaveState = settings.getBoolean("save_state", false);

        if (savedInstanceState != null)
            if (savedInstanceState.getBoolean("hasState"))
                load();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);

        frameLayout.addView(view);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        // Create the client used to sign in to Google services.
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        if(!isSignedIn())
            startSignInIntent();

        // check dialog shown:
        if(!isNewFeaturesDialogShowed())
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.new_features_title)
                    .setPositiveButton(R.string.new_features_positive_btn, (dialog, which) -> turnOffNewFeaturesDialogShowed())
                    .setMessage(R.string.message_new_features)
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_MENU)
            return true;
        else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
        {
            view.game.move(2);
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
        {
            view.game.move(0);
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
        {
            view.game.move(3);
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
        {
            view.game.move(1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putBoolean("hasState", true);
        save();
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onPause()
    {
        super.onPause();
        save();

        if(isSignedIn())
        {
            pushAccomplishments();
            updateLeaderboards();
        }
    }

    protected void onResume()
    {
        super.onResume();
        load();

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();

        if(isSignedIn())
        {
            pushAccomplishments();
            updateLeaderboards();
        }
    }

    private boolean isNewFeaturesDialogShowed()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        return settings.getBoolean("has_new_dialog_showed_1", false);
    }

    private void turnOffNewFeaturesDialogShowed()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("has_new_dialog_showed_1", true);
        editor.apply();
    }

    private void save() {
        final int rows;
        if(MainMenuActivity.getRows() != 0)
            rows= MainMenuActivity.getRows();
        else
            rows= MainMenuActivityMadness.getRows();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        Tile[][] field = view.game.grid.field;
        Tile[][] undoField = view.game.grid.undoField;
        editor.putInt(WIDTH + rows, field.length);
        editor.putInt(HEIGHT + rows, field.length);

        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] != null)
                    editor.putInt(rows + " " + xx + " " + yy, field[xx][yy].getValue());
                else
                    editor.putInt(rows + " " + xx + " " + yy, 0);

                if (undoField[xx][yy] != null)
                    editor.putInt(UNDO_GRID + rows + " " + xx + " " + yy, undoField[xx][yy].getValue());
                else
                    editor.putInt(UNDO_GRID + rows + " " + xx + " " + yy, 0);
            }
        }

        // reward deletions:
        editor.putInt(REWARD_DELETES + rows, mRewardDeletes);
        editor.putInt(REWARD_DELETE_SELECTION + rows, mRewardDeletingSelectionAmounts);

        // game values:
        editor.putLong(SCORE + rows, view.game.score);
        editor.putLong(HIGH_SCORE + rows, view.game.highScore);
        editor.putLong(UNDO_SCORE + rows, view.game.lastScore);
        editor.putBoolean(CAN_UNDO + rows, view.game.canUndo);
        editor.putInt(GAME_STATE + rows, view.game.gameState);
        editor.putInt(UNDO_GAME_STATE + rows, view.game.lastGameState);
        editor.apply();

        // my reason for writing this operation here: i want take effect after save()
        if (MainMenuActivity.getRows() != 0) {
            switch (MainMenuActivity.getRows())
            {
                case 4:
                    mHighScore4x4 = view.game.highScore;
                    break;
                case 5:
                    mHighScore5x5 = view.game.highScore;
                    break;
                case 7:
                    mHighScore6x6 = view.game.highScore;
                    break;
                case 8:
                    mHighScore8x8 = view.game.highScore;
                    break;
                case 9:
                    mHighScore11x11 = view.game.highScore;
                    break;
                case 10:
                    mHighScore15x15 = view.game.highScore;
                    break;
                case 11:
                    mHighScore25x25 = view.game.highScore;
                    break;
                case 12:
                    mHighScore50x50 = view.game.highScore;
                    break;
                case 13:
                    mHighScore100x100 = view.game.highScore;
                    break;

            }
        }
        else {
            switch (MainMenuActivityMadness.getRows())
            {
                case 4:
                    mHighScore4x4 = view.game.highScore;
                    break;
                case 5:
                    mHighScore5x5 = view.game.highScore;
                    break;
                case 7:
                    mHighScore6x6 = view.game.highScore;
                    break;
                case 8:
                    mHighScore8x8 = view.game.highScore;
                    break;
                case 9:
                    mHighScore11x11 = view.game.highScore;
                    break;
                case 10:
                    mHighScore15x15 = view.game.highScore;
                    break;
                case 11:
                    mHighScore25x25 = view.game.highScore;
                    break;
                case 12:
                    mHighScore50x50 = view.game.highScore;
                    break;
                case 13:
                    mHighScore100x100 = view.game.highScore;
                    break;

            }
        }
    }

    private void load()
    {
        final int rows;

        if (MainMenuActivity.getRows() != 0)
            rows = MainMenuActivity.getRows();
        else
            rows = MainMenuActivityMadness.getRows();


        //Stopping all animations
        view.game.aGrid.cancelAnimations();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        for (int xx = 0; xx < view.game.grid.field.length; xx++)
        {
            for (int yy = 0; yy < view.game.grid.field[0].length; yy++)
            {
                int value = settings.getInt( rows + " " + xx + " " + yy, -1);
                if (value > 0)
                    view.game.grid.field[xx][yy] = new Tile(xx, yy, value);
                else if (value == 0)
                    view.game.grid.field[xx][yy] = null;

                int undoValue = settings.getInt(UNDO_GRID + rows + " " + xx + " " + yy, -1);
                if (undoValue > 0)
                    view.game.grid.undoField[xx][yy] = new Tile(xx, yy, undoValue);
                else if (value == 0)
                    view.game.grid.undoField[xx][yy] = null;
            }
        }

        mRewardDeletes = settings.getInt(REWARD_DELETES + rows, 2);
        mRewardDeletingSelectionAmounts = settings.getInt(REWARD_DELETE_SELECTION + rows, 3);

        view.game.score = settings.getLong(SCORE + rows, view.game.score);
        view.game.highScore = settings.getLong(HIGH_SCORE + rows, view.game.highScore);
        view.game.lastScore = settings.getLong(UNDO_SCORE + rows, view.game.lastScore);
        view.game.canUndo = settings.getBoolean(CAN_UNDO + rows, view.game.canUndo);
        view.game.gameState = settings.getInt(GAME_STATE + rows, view.game.gameState);
        view.game.lastGameState = settings.getInt(UNDO_GAME_STATE + rows, view.game.lastGameState);
    }

    @SuppressLint("VisibleForTests")
    public void loadAndPrintEvents()
    {
        mEventsClient.load(true).addOnSuccessListener(eventBufferAnnotatedData -> {
            EventBuffer eventBuffer = eventBufferAnnotatedData.get();

            int count = 0;
            if (eventBuffer != null)
                count = eventBuffer.getCount();

            for (int i = 0; i < count; i++) {
                Event event = eventBuffer.get(i);
                Log.i(TAG, "event: "
                        + event.getName()
                        + " -> "
                        + event.getValue());
            }
        })
                .addOnFailureListener(e -> handleException(e, getString(R.string.achievements_exception)));
    }

    public void signInSilently()
    {
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this, task -> {
            if (task.isSuccessful())
                onConnected(task.getResult());
            else
                onDisconnected();
        });
    }

    public void handleException(Exception e, String details)
    {
        int status = 0;

        if (e instanceof ApiException)
        {
            ApiException apiException = (ApiException) e;
            status = apiException.getStatusCode();
        }

        getString(R.string.status_exception_error, details, status, e);
    }

    public void pushAccomplishments()
    {
        if (!isSignedIn())
            return; // can't push to the cloud, try again later

        try
        {
            if (mAchievement32)
            {
                mAchievementsClient.unlock(getString(R.string.achievement_32));
                mAchievement32 = false;
            }

            if (mAchievement64)
            {
                mAchievementsClient.unlock(getString(R.string.achievement_64));
                mAchievement64 = false;
            }

            if (mAchievement128)
            {
                mAchievementsClient.unlock(getString(R.string.achievement_128));
                mAchievement128 = false;
            }

            if (mAchievement256)
            {
                mAchievementsClient.unlock(getString(R.string.achievement_256));
                mAchievement256 = false;
            }

            if (mAchievement512)
            {
                mAchievementsClient.unlock(getString(R.string.achievement_512));
                mAchievement512 = false;
            }

            if (mAchievement1024)
            {
                mAchievementsClient.unlock(getString(R.string.achievement_1024));
                mAchievement1024 = false;
            }

            if (mAchievement2048)
            {
                mAchievementsClient.unlock(getString(R.string.achievement_2048));
                mAchievement2048 = false;
            }

            if (mAchievement4096)
            {
                mAchievementsClient.unlock(getString(R.string.achievement_4096));
                mAchievement4096 = false;
            }

            if (mAchievement8192)
            {
                mAchievementsClient.unlock(getString(R.string.achievement_8192));
                mAchievement8192 = false;
            }
        }
        catch (Exception e)
        {
            Log.e("PushAccomplishments", Arrays.toString(e.getStackTrace()));
        }
    }

    private void updateLeaderboards()
    {
        if (!isSignedIn())
            return; // can't push to the cloud, try again later

        try
        {
            if (mHighScore4x4 >= 0)
            {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_4x4), mHighScore4x4);
                mHighScore4x4 = -1;
            }

            if (mHighScore5x5 >= 0)
            {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_5x5), mHighScore5x5);
                mHighScore5x5 = -1;
            }

            if (mHighScore6x6 >= 0)
            {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_6x6), mHighScore6x6);
                mHighScore6x6 = -1;
            }

            if (mHighScore8x8 >= 0)
            {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_8x8), mHighScore8x8);
                mHighScore8x8 = -1;
            }

            if (mHighScore11x11 >= 0)
            {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_11x11), mHighScore11x11);
                mHighScore11x11 = -1;
            }

            if (mHighScore15x15 >= 0)
            {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_15x15), mHighScore15x15);
                mHighScore15x15 = -1;
            }

            if (mHighScore25x25 >= 0)
            {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_25x25), mHighScore25x25);
                mHighScore25x25 = -1;
            }

            if (mHighScore50x50 >= 0)
            {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_50x50), mHighScore50x50);
                mHighScore50x50 = -1;
            }

            if (mHighScore100x100 >= 0)
            {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_100x100), mHighScore100x100);
                mHighScore100x100 = -1;
            }
        }
        catch (Exception e)
        {
            Log.e("updateLeaderboards", Arrays.toString(e.getStackTrace()));
        }
    }

    @SuppressLint("VisibleForTests")
    public void onConnected(GoogleSignInAccount googleSignInAccount)
    {
        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mEventsClient = Games.getEventsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

        // Set the greeting appropriately on main menu
        mPlayersClient.getCurrentPlayer().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                task.getResult().getDisplayName();
            else
            {
                Exception e = task.getException();
                handleException(e, getString(R.string.players_exception));
            }
        });

        // if we have accomplishments to push, push them
        if (!isEmptyAchievementsOrLeaderboards())
        {
            pushAccomplishments();
            updateLeaderboards();
        }
        loadAndPrintEvents();
    }

    public void onDisconnected()
    {
        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mPlayersClient = null;
    }

    private void startSignInIntent()
    {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    private boolean isSignedIn()
    {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);

            try
            {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            }
            catch (ApiException apiException)
            {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty())
                    message = getString(R.string.signin_other_error);

                onDisconnected();

                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    public static void unlockAchievement(int requestedTile)
    {
        // Check if each condition is met; if so, unlock the corresponding achievement.
        if (requestedTile == 32) mAchievement32 = true;
        if (requestedTile == 64) mAchievement64 = true;
        if (requestedTile == 128) mAchievement128 = true;
        if (requestedTile == 256) mAchievement256 = true;
        if (requestedTile == 512) mAchievement512 = true;
        if (requestedTile == 1024) mAchievement1024 = true;
        if (requestedTile == 2048) mAchievement2048 = true;
        if (requestedTile == 4096) mAchievement4096 = true;
        if (requestedTile == 8192) mAchievement8192 = true;
    }

    private boolean isEmptyAchievementsOrLeaderboards()
    {
        return !mAchievement32 || !mAchievement64 || !mAchievement128 || !mAchievement256
                || !mAchievement512 || !mAchievement1024 || !mAchievement2048 || !mAchievement4096
                || !mAchievement8192 || mHighScore4x4 < 0 || mHighScore5x5 < 0 || mHighScore6x6 < 0
                || mHighScore8x8 < 0 || mHighScore11x11 < 0 || mHighScore15x15 < 0 || mHighScore25x25 < 0
                || mHighScore50x50 < 0 || mHighScore100x100 < 0;
    }
}