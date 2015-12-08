package com.steelcomputers.android.assignmenttwo;

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
import java.util.List;

/**
 * PlayerDetailFragment.java
 *
 * A fragment representing a single Contestant detail screen.
 * This fragment is either contained in a {@link PlayerListActivity}
 * in two-pane mode (on tablets) or a {@link PlayerDetailActivity}
 * on handsets.
 *
 * Created by John Steel on 2015-10-31.
 */
public class PlayerDetailFragment extends Fragment implements Contestant.PlayerListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_STATE_SAVED = "state_saved";
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The dummy content this fragment is presenting.
     */
    private Contestant mContestant;
    private CollapsingToolbarLayout mAppBar;
    private TextView mWins;
    private TextView mLosses;
    private TextView mTies;
    private Button mBtnRename;
    private Button mBtnDelete;
    private Button mBtnChallenge;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( savedInstanceState != null && savedInstanceState.getBoolean(ARG_STATE_SAVED)) {
            mContestant = (Contestant) savedInstanceState.getSerializable(ARG_ITEM_ID);
        } else if (getArguments().containsKey(ARG_ITEM_ID)) {
            mContestant = Contestant.getPlayers().get(getArguments().getInt(ARG_ITEM_ID));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_STATE_SAVED, true);
        outState.putSerializable(ARG_ITEM_ID, mContestant);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player_detail, container, false);

        mAppBar = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        mWins = (TextView) rootView.findViewById(R.id.player_wins);
        mLosses = (TextView) rootView.findViewById(R.id.player_losses);
        mTies = (TextView) rootView.findViewById(R.id.player_ties);
        mBtnRename = ((Button) rootView.findViewById(R.id.player_rename));
        mBtnDelete = ((Button) rootView.findViewById(R.id.player_delete));
        mBtnChallenge = ((Button) rootView.findViewById(R.id.player_challenge));

        mBtnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContestant.getRenamePlayerDialog(getActivity()).show();
            }
        });

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContestant.getDeletePlayerDialog(getActivity()).show();
            }
        });

        mBtnChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<Contestant> contestants = new ArrayList<Contestant>();
                contestants.addAll(Contestant.getPlayers()); // Can't use reference because I need to change it
                contestants.remove(mContestant);
                mContestant.getPlayersDialog(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCallbacks.onStartMatch(mContestant, contestants.get(which));
                        dialog.dismiss();
                    }
                }, contestants).show();
            }
        });

        setViewValues();
        return rootView;
    }

    @Override
    public void notifyChange(List<Contestant> contestants) {
        setViewValues();
    }

    private void setViewValues() {
        try {
            if (mContestant != null) {
                if (Contestant.getPlayers().contains(mContestant)) {
                    if (mAppBar != null) {
                        mAppBar.setTitle(mContestant.getName());
                    }

                    mWins.setText(Integer.toString(mContestant.getWins()));
                    mLosses.setText(Integer.toString(mContestant.getLosses()));
                    mTies.setText(Integer.toString(mContestant.getTies()));

                    String playerType = " " + mContestant.DefineIfPlayerOrTeam();

                    mBtnRename.setText(this.getResources().getString(R.string.player_rename) + playerType);
                    mBtnDelete.setText(this.getResources().getString(R.string.player_delete) + playerType);

                } else {
                    if (mAppBar != null) {
                        mAppBar.setTitle("(Deleted) " + mContestant.getName());
                    }

                    mWins.setText("-");
                    mLosses.setText("-");
                    mTies.setText("-");

                    mBtnRename.setEnabled(false);
                    mBtnDelete.setEnabled(false);
                    mBtnChallenge.setEnabled(false);
                }
            }
        } catch (Exception e) {
            android.util.Log.e(getClass().getName(), "Failed to set fragment values", e);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Contestant.addListener(this);

        // Activities containing this fragment must implement its callbacks.
        if (!(getActivity() instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Contestant.removeListener(this);
        mCallbacks = sDummyCallbacks;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onStartMatch(Contestant contestantOne, Contestant contestantTwo);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onStartMatch(Contestant contestantOne, Contestant contestantTwo) {
        }
    };
}
