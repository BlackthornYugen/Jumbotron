package com.steelcomputers.android.assignmenttwo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a single Player detail screen.
 * This fragment is either contained in a {@link PlayerListActivity}
 * in two-pane mode (on tablets) or a {@link PlayerDetailActivity}
 * on handsets.
 */
public class PlayerDetailFragment extends Fragment implements Player.PlayerListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Player mPlayer;
    private CollapsingToolbarLayout mAppBar;
    private TextView mWins;
    private TextView mLosses;
    private TextView mTies;
    private Button mBtnRename;
    private Button mBtnDelete;
    private Button mBtnChallange;

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
            mAppBar = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (mAppBar != null) {
                mAppBar.setTitle(mPlayer.getName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player_detail, container, false);

        mWins = (TextView) rootView.findViewById(R.id.player_wins);
        mLosses = (TextView) rootView.findViewById(R.id.player_losses);
        mTies = (TextView) rootView.findViewById(R.id.player_ties);
        mBtnRename = ((Button) rootView.findViewById(R.id.player_rename));
        mBtnDelete = ((Button) rootView.findViewById(R.id.player_delete));
        mBtnChallange = ((Button) rootView.findViewById(R.id.player_challenge));

        mBtnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.getRenamePlayerDialog(getActivity()).show();
            }
        });

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.getDeletePlayerDialog(getActivity()).show();
            }
        });

        mBtnChallange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Player> players = new ArrayList<Player>();
                players.addAll(Player.getPlayers()); // Can't use reference because I need to change it
                players.remove(mPlayer);
                mPlayer.getPlayersDialog(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.util.Log.d(getClass().getName(), String.format("Selected: %s", which));
                        dialog.dismiss();
                    }
                }, players).show();
            }
        });

        setViewValues();
        return rootView;
    }

    @Override
    public void notifyChange(List<Player> players) {
        setViewValues();
    }

    private void setViewValues() {
        if (mPlayer != null) {
            if (Player.getPlayers().contains(mPlayer)) {
                if (mAppBar != null) {
                    mAppBar.setTitle(mPlayer.getName());
                }

                mWins.setText(Integer.toString(mPlayer.getWins()));
                mLosses.setText(Integer.toString(mPlayer.getLosses()));
                mTies.setText(Integer.toString(mPlayer.getTies()));
            } else {
                if (mAppBar != null) {
                    mAppBar.setTitle(mPlayer.getName() + " (Deleted)");
                }

                mWins.setText("-");
                mLosses.setText("-");
                mTies.setText("-");

                mBtnRename.setEnabled(false);
                mBtnDelete.setEnabled(false);
                mBtnChallange.setEnabled(false);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Player.addListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Player.removeListener(this);
    }
}
