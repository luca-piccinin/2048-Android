package com.gameditors.TheOrginal2048;

import static android.content.ContentValues.TAG;

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
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
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


public class MainMenuActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    public static boolean mIsMainMenu = true;

    private static int mRows = 0;

    public static int getRows() {
        return mRows;
    }

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

    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
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

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });

        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.");
                mInterstitialAd = null;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.");
                mInterstitialAd = null;
            }

            @Override
            public void onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.");
            }
        });


        mIsMainMenu = true;

        Typeface ClearSans_Bold = Typeface.createFromAsset(getResources().getAssets(), "ClearSans-Bold.ttf");

        Button bt4x4 = findViewById(R.id.btn_start_4x4);
        Button bt5x5 = findViewById(R.id.btn_start_5x5);
        Button bt6x6 = findViewById(R.id.btn_start_6x6);
        Button bt8x8 = findViewById(R.id.btn_start_8x8);
        Button bt11x11 = findViewById(R.id.btn_start_11x11);
        Button bt15x15 = findViewById(R.id.btn_start_15x15);
        Button btMadness = findViewById(R.id.btn_madness);

        bt4x4.setTypeface(ClearSans_Bold);
        bt5x5.setTypeface(ClearSans_Bold);
        bt6x6.setTypeface(ClearSans_Bold);
        bt8x8.setTypeface(ClearSans_Bold);
        bt11x11.setTypeface(ClearSans_Bold);
        bt15x15.setTypeface(ClearSans_Bold);
        btMadness.setTypeface(ClearSans_Bold);

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
                startActivity(new Intent(MainMenuActivity.this, ColorPickerActivity.class));
                break;
            case R.id.settings_sign_out:
                signOut();
                break;
        }
        return false;
    }

    // Buttons:
    @SuppressLint({"NonConstantResourceId", "IntentReset"})
    public void onButtonsClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_start_4x4:
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                this.StartGame(4);
                break;
            case R.id.btn_start_5x5:
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                this.StartGame(5);
                break;
            case R.id.btn_start_6x6:
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                this.StartGame(6);
                break;
            case R.id.btn_start_8x8:
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                this.StartGame(8);
                break;
            case R.id.btn_start_11x11:
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                this.StartGame(11);
                break;
            case R.id.btn_start_15x15:
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                this.StartGame(15);
                break;
            case R.id.btn_madness:
                CustomDialog dialog = new CustomDialog(this, getResources().getString(R.string.message));
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
                    Toast.makeText(MainMenuActivity.this, getString(R.string.email_client_error), Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mIsMainMenu = true;

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
        mIsMainMenu = false;
        startActivity(new Intent(MainMenuActivity.this, MainActivity.class));
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