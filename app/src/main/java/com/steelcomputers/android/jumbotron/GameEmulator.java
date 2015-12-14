package com.steelcomputers.android.jumbotron;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;

/**
 * GameEmulator.java
 *
 * Allow our users to update Wins, Ties & Losses via {@link GameEmulatorFragment}
 *
 * Created by John Steel on 2015-11-02 from a template.
 */
public class GameEmulator extends AppCompatActivity {
    private static final String TAG = GameEmulator.class.getSimpleName();
    private CastScoreService.ScoreBinder mCastService;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter mMediaRouter;
    private MediaRouter.Callback mRouteCallbacks;
    private int mHomeIndex = 0;
    private int mAwayIndex = 0;
    private ServiceConnection mServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHomeIndex = getIntent().getIntExtra(GameEmulatorFragment.ARG_PLAYER_ONE, 0);
        mAwayIndex = getIntent().getIntExtra(GameEmulatorFragment.ARG_PLAYER_TWO, 0);

        try {
            setContentView(R.layout.activity_game_emulator);
            Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (savedInstanceState == null) {
                // Create the detail fragment and add it to the activity
                // using a fragment transaction.
                Bundle arguments = new Bundle();
                arguments.putInt(GameEmulatorFragment.ARG_PLAYER_ONE, mHomeIndex);
                arguments.putInt(GameEmulatorFragment.ARG_PLAYER_TWO, mAwayIndex);
                GameEmulatorFragment fragment = new GameEmulatorFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.game_fragment_container, fragment)
                        .commit();
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't add fragment", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();        // Bind cast service
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected");
                mCastService = (CastScoreService.ScoreBinder) service;

                // Start media router discovery
                mMediaRouter.addCallback(
                        mMediaRouteSelector,
                        new MediaRouter.Callback() {
                            @Override
                            public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
                                super.onRouteSelected(router, route);
                                Log.d(TAG, "onRouteSelected");
                                // Handle the user route selection.
                                mCastService.launchReceiver(CastDevice.getFromBundle(route.getExtras()));
                                mCastService.watchGame(Contestant.getPlayers().get(mHomeIndex),
                                                       Contestant.getPlayers().get(mAwayIndex));
                                mCastService.sendMessage("Awaiting scores...");
                            }

                            @Override
                            public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
                                super.onRouteUnselected(router, route);
                                Log.d(TAG, "onRouteUnselected: info=" + route);
                                mCastService.teardown();
                            }
                        },
                        MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected");
                mCastService = null;
            }
        };
        Intent intent = new Intent(GameEmulator.this, CastScoreService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        // Configure Cast device discovery
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(getResources()
                        .getString(R.string.app_id))).build();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game_emulator, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider
                = (MediaRouteActionProvider) MenuItemCompat
                .getActionProvider(mediaRouteMenuItem);
        // Set the MediaRouteActionProvider selector for device discovery.
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                navigateUpTo(new Intent(this, PlayerListActivity.class));
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_refresh:
                if (Contestant.isRunningAQuery()) {
                    Toast.makeText(this, R.string.player_refresh_running, Toast.LENGTH_SHORT).show();
                } else {
                    Contestant.queryPlayers();
                    boolean sync_data = Preferences.getSharedPreferences().getBoolean("data_sync", false);
                    Toast.makeText(this, sync_data ? R.string.refresh_remote : R.string.refresh_remote, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        mRouteCallbacks = null;
    }
}
