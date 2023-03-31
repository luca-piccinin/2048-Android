package com.gameditors.TheOrginal2048;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.tasks.Task;


public class MainMenuActivityMadness extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener
{

    private static int mRows = 25;
    public static int getRows() { return mRows; }

    private final String BACKGROUND_COLOR_KEY = "BackgroundColor";
    public static int mBackgroundColor = 0;

    // Client used to sign in with Google APIs
    public GoogleSignInClient mGoogleSignInClient;

    // Client variables
    public AchievementsClient mAchievementsClient;
    public LeaderboardsClient mLeaderboardsClient;
    public EventsClient mEventsClient;
    public PlayersClient mPlayersClient;

    // request codes we use when invoking an external activity
    public static final int RC_UNUSED = 5001;
    public static final int RC_SIGN_IN = 9001;

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            // Network is available
            initializeMobileAds();
        }

        @Override
        public void onLost(Network network) {
            // Network is lost
        }
    };

    private void initializeMobileAds() {
        MobileAds.initialize(this, initializationStatus -> {
            // Mobile Ads initialization complete
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_madness);


        Typeface ClearSans_Bold = Typeface.createFromAsset(getResources().getAssets(), "ClearSans-Bold.ttf");

        Button bt25x25 = findViewById(R.id.btn_start_25x25);
        Button bt50x50 = findViewById(R.id.btn_start_50x50);
        Button bt100x100 = findViewById(R.id.btn_start_100x100);
        Button btback = findViewById(R.id.btn_back);

        bt25x25.setTypeface(ClearSans_Bold);
        bt50x50.setTypeface(ClearSans_Bold);
        bt100x100.setTypeface(ClearSans_Bold);
        btback.setTypeface(ClearSans_Bold);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);

        // Create the client used to sign in to Google services.
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        if(isSignedIn())
            startSignInIntent();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings_color_picker:
                mRows = 4;  // because of its GameView!
                startActivity(new Intent(MainMenuActivityMadness.this, ColorPickerActivity.class));
                break;
            case R.id.settings_sign_out:
                signOut();
                break;
        }
        return false;
    }

    @SuppressLint({"NonConstantResourceId", "IntentReset"})
    public void onButtonsClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_start_25x25:
                this.StartGame(25);
                break;
            case R.id.btn_start_50x50:
                this.StartGame(50);
                break;
            case R.id.btn_start_100x100:
                this.StartGame(100);
                break;
            case R.id.btn_back:
                CustomDialog dialog = new CustomDialog(this, getResources().getString(R.string.message_madness));
                dialog.show();
                break;
            case R.id.btn_show_achievements:
                if(isSignedIn())
                    startSignInIntent();
                else
                {
                    try
                    {
                        onShowAchievementsRequested();
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.btn_show_leaderboards:
                if(isSignedIn())
                    startSignInIntent();
                else
                {
                    try
                    {
                        onShowLeaderboardsRequested();
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.btn_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.get_from_bazaar) + "\n\n" + getString(R.string.url_google_play));

                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
                break;
            case R.id.btn_more_games:
                try
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store/apps/dev?id=8880176094509043816"));
                    startActivity(intent);
                }
                catch (Exception e)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_google_play))));
                }
                break;
            case R.id.btn_rate:
                Intent bazaarIntent = new Intent(Intent.ACTION_EDIT);
                bazaarIntent.setData(Uri.parse("https://play.google.com/store/apps/dev?id=8880176094509043816"));

                try
                {
                    startActivity(bazaarIntent);
                }
                catch (Exception e) // for activity not found exception
                {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_google_play))));
                }
                break;
            case R.id.btn_social_instagram:
                Intent instagramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.instagram_page_uri)));
                instagramIntent.setPackage(getString(R.string.instagram_package_name));

                try
                {
                    startActivity(instagramIntent);
                }
                catch (ActivityNotFoundException e)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.instagram_page_uri))));
                }
                catch (Exception e)
                {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_settings:
                PopupMenu popup = new PopupMenu(this,view);
                popup.setOnMenuItemClickListener(this);// to implement on click event on items of menu
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menus, popup.getMenu());
                popup.show();
                break;
            case R.id.btn_send_email:
                String[] TO = { getString(R.string.email_support_address) };
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");

                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));

                try
                {
                    emailIntent.setPackage("com.google.android.gm");
                    startActivity(emailIntent);
                }
                catch (ActivityNotFoundException ex)
                {
                    emailIntent.setPackage("");
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.email_send_title)));
                }
                catch (Exception e)
                {
                    Toast.makeText(MainMenuActivityMadness.this, getString(R.string.email_client_error), Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();

        SaveColors();
        LoadColors();
    }

    private void SaveColors()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();

        if(mBackgroundColor < 0)
            editor.putInt(BACKGROUND_COLOR_KEY, mBackgroundColor);

        editor.apply();
    }

    private void LoadColors()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if(settings.getInt(BACKGROUND_COLOR_KEY, mBackgroundColor) < 0)
            mBackgroundColor = settings.getInt(BACKGROUND_COLOR_KEY, mBackgroundColor);
        else
            mBackgroundColor = ContextCompat.getColor(this, R.color.colorBackground);
    }

    private void StartGame(int rows)
    {
        mRows = rows;
        startActivity(new Intent(MainMenuActivityMadness.this, MainActivity.class));
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

    private void signOut()
    {
        if (isSignedIn())
            return;

        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            task.isSuccessful();

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
        return GoogleSignIn.getLastSignedInAccount(this) == null;
    }

    public void onShowAchievementsRequested()
    {
        mAchievementsClient.getAchievementsIntent().addOnSuccessListener(intent -> startActivityForResult(intent, RC_UNUSED)).addOnFailureListener(e -> handleException(e, getString(R.string.achievements_exception)));
    }

    public void onShowLeaderboardsRequested()
    {
        mLeaderboardsClient.getAllLeaderboardsIntent().addOnSuccessListener(intent -> startActivityForResult(intent, RC_UNUSED)).addOnFailureListener(e -> handleException(e, getString(R.string.leaderboards_exception)));
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
}