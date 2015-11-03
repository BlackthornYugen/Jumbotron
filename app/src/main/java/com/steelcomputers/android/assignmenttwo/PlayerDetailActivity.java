package com.steelcomputers.android.assignmenttwo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


/**
 * PlayerDetailActivity.java
 *
 * Extends {@link PlayerDetailFragment.Callbacks} so that the fragment can let the
 * activity know when it wants a match to be started.
 *
 * Created by John Steel on 2015-10-31.
 */
public class PlayerDetailActivity
        extends AppCompatActivity
        implements PlayerDetailFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_player_detail);
            Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (savedInstanceState == null) {
                Bundle arguments = new Bundle();
                arguments.putInt(PlayerDetailFragment.ARG_ITEM_ID,
                        getIntent().getIntExtra(PlayerDetailFragment.ARG_ITEM_ID, 0));
                PlayerDetailFragment fragment = new PlayerDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.player_detail_container, fragment)
                        .commit();
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Failure during activity creation.", e);
        }
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
     * Callback method from {@link PlayerDetailFragment.Callbacks}
     * indicating that a match should start.
     */
    @Override
    public void onStartMatch(Player playerOne, Player playerTwo) {
        Intent gameEmulator = new Intent(this, GameEmulator.class);
        gameEmulator.putExtra(GameEmulatorFragment.ARG_PLAYER_ONE, Player.getPlayers().indexOf(playerOne));
        gameEmulator.putExtra(GameEmulatorFragment.ARG_PLAYER_TWO, Player.getPlayers().indexOf(playerTwo));
        startActivity(gameEmulator);
    }
}
