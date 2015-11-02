package com.steelcomputers.android.assignmenttwo;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * A fragment representing a single Player detail screen.
 * This fragment is either contained in a {@link PlayerListActivity}
 * in two-pane mode (on tablets) or a {@link PlayerDetailActivity}
 * on handsets.
 */
public class PlayerDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Player mPlayer;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mPlayer = Player.getPlayers().get(getArguments().getInt(ARG_ITEM_ID));
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mPlayer.getName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mPlayer != null) {
            ((TextView) rootView.findViewById(R.id.player_name))
                    .setText(mPlayer.getName());
            ((TextView) rootView.findViewById(R.id.player_wins))
                    .setText(Integer.toString(mPlayer.getWins()));
            ((TextView) rootView.findViewById(R.id.player_losses))
                    .setText(Integer.toString(mPlayer.getLosses()));
            ((TextView) rootView.findViewById(R.id.player_ties))
                    .setText(Integer.toString(mPlayer.getTies()));
        }

        return rootView;
    }
}
