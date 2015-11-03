package com.steelcomputers.android.assignmenttwo;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.MenuItem;

/**
 * An activity representing a single Player detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PlayerListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link PlayerDetailFragment}.
 */
public class PlayerDetailActivity
        extends AppCompatActivity
        implements PlayerDetailFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.challange);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(PlayerDetailFragment.ARG_ITEM_ID,
                    getIntent().getIntExtra(PlayerDetailFragment.ARG_ITEM_ID, 0));
            PlayerDetailFragment fragment = new PlayerDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.player_detail_container, fragment)
                    .commit();
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
