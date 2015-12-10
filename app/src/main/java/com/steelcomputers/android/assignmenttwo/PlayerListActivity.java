package com.steelcomputers.android.assignmenttwo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;


/**
 * PlayerListActivity.java
 *
 * An activity representing a list of Players. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PlayerDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PlayerListFragment} and the item details
 * (if present) is a {@link PlayerDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link PlayerListFragment.Callbacks} interface
 * to listen for item selections.
 *
 * Created by John Steel on 2015-10-31 from a template.
 */
public class PlayerListActivity extends AppCompatActivity
        implements PlayerListFragment.Callbacks, PlayerDetailFragment.Callbacks,
        SwipeRefreshLayout.OnRefreshListener, Contestant.PlayerListener {

    public static final int NETWORK_TIMEOUT_MILLIS = 5000;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mSelection;

    @Override
    public void onRefresh() {
        final PlayerListActivity context = this;
        if (Contestant.isRunningAQuery()) {
            Toast.makeText(context, R.string.player_refresh_running, Toast.LENGTH_SHORT).show();
        } else {
            Contestant.addListener(context);
            Contestant.queryPlayers();
            boolean sync_data = Preferences.getSharedPreferences().getBoolean("cloud_sync", false);
            Toast.makeText(this, sync_data ? R.string.refresh_remote : R.string.refresh_local, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(context, getString(R.string.timeout), Toast.LENGTH_LONG).show();
                        Contestant.removeListener(context);
                    }
                }
            }, NETWORK_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void notifyChange(List<Contestant> contestants) {
        // Stop refresh animation because a change happened.
        mSwipeRefreshLayout.setRefreshing(false);
        Contestant.removeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PlayerListActivity context = this;
        setContentView(R.layout.activity_player_app_bar);

        Preferences.setContext(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


        FloatingActionButton fabAddPlayer = (FloatingActionButton) findViewById(R.id.fabAddPlayer);
        fabAddPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contestant.getNewPlayerDialog(context, 0).show();
            }
        });

        FloatingActionButton fabAddTeam = (FloatingActionButton) findViewById(R.id.fabAddTeam);
        fabAddTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contestant.getNewPlayerDialog(context, 1).show();
            }
        });

        FloatingActionButton fabDeleteAllPlayers = (FloatingActionButton) findViewById(R.id.fabDeleteAllPlayer);
        fabDeleteAllPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contestant.deleteAllPlayers();
                Toast.makeText(context, getResources().getString(R.string.all_players_deleted), Toast.LENGTH_LONG).show();
            }
        });



        try {
            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
            mSwipeRefreshLayout.setOnRefreshListener(this);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Can't set refresh listener", e);
        }

        if (findViewById(R.id.player_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((PlayerListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.player_list))
                    .setActivateOnItemClick(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_refresh:
                onRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_players, menu);
        return true;
    }

    /**
     * Callback method from {@link PlayerListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onPlayerSelected(String id) {
        int playerId = Integer.parseInt(id);

        if (mTwoPane) {
            if (id.equals(mSelection)) {
                // Rename player if selected twice in two-pane mode
                Contestant.getPlayers().get(playerId).getRenamePlayerDialog(this).show();
            } else {
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.
                Bundle arguments = new Bundle();
                arguments.putInt(PlayerDetailFragment.ARG_ITEM_ID, playerId);
                PlayerDetailFragment fragment = new PlayerDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.player_detail_container, fragment)
                        .commit();
            }
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, PlayerDetailActivity.class);
            detailIntent.putExtra(PlayerDetailFragment.ARG_ITEM_ID, playerId);
            startActivity(detailIntent);
        }
        mSelection = id;
    }

    /**
     * Callback method from {@link PlayerDetailFragment.Callbacks}
     * indicating that a match should start. Only called on two-pane
     * layouts. {@link PlayerDetailActivity} handles it on one-pane
     * layouts.
     */
    @Override
    public void onStartMatch(Contestant contestantOne, Contestant contestantTwo) {
        Bundle arguments = new Bundle();

        arguments.putInt(GameEmulatorFragment.ARG_PLAYER_ONE, Contestant.getPlayers().indexOf(contestantOne));
        arguments.putInt(GameEmulatorFragment.ARG_PLAYER_TWO, Contestant.getPlayers().indexOf(contestantTwo));
        GameEmulatorFragment fragment = new GameEmulatorFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_detail_container, fragment)
                .commit();
    }
}
