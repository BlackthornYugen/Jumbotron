package com.steelcomputers.android.assignmenttwo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

public class GameEmulator extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_game_emulator);
            Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
            setSupportActionBar(toolbar);

            //   Show the Up button in the action bar.
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
                arguments.putInt(GameEmulatorFragment.ARG_PLAYER_ONE,
                        getIntent().getIntExtra(GameEmulatorFragment.ARG_PLAYER_ONE, 0));
                arguments.putInt(GameEmulatorFragment.ARG_PLAYER_TWO,
                        getIntent().getIntExtra(GameEmulatorFragment.ARG_PLAYER_TWO, 0));
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

}
