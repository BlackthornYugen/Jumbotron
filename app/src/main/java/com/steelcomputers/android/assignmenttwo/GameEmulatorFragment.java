package com.steelcomputers.android.assignmenttwo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * GameEmulatorFragment.java
 *
 * A placeholder fragment containing a simple view.
 *
 * Created by John Steel on 2015-11-02 from a template.
 */
public class GameEmulatorFragment extends Fragment implements Contestant.PlayerListener {

    public static final String ARG_PLAYER_ONE = "player_one";
    public static final String ARG_PLAYER_TWO = "player_two";
    public static final String ARG_STATE_SAVED = "state_saved";
    private Contestant[] mContestant = new Contestant[2];
    private Button[] mBtnWinner = new Button[2];
    private TextView[] mTxtName;
    private TextView[] mTxtWins;
    private TextView[] mTxtTies;
    private TextView[] mTxtLoss;

    public GameEmulatorFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( savedInstanceState != null && savedInstanceState.getBoolean(ARG_STATE_SAVED)) {
            mContestant[0] = (Contestant) savedInstanceState.getSerializable(ARG_PLAYER_ONE);
            mContestant[1] = (Contestant) savedInstanceState.getSerializable(ARG_PLAYER_TWO);
        } else {
            if (getArguments().containsKey(ARG_PLAYER_ONE)) {
                mContestant[0] = Contestant.getPlayers().get(getArguments().getInt(ARG_PLAYER_ONE));
            }
            if (getArguments().containsKey(ARG_PLAYER_TWO)) {
                mContestant[1] = Contestant.getPlayers().get(getArguments().getInt(ARG_PLAYER_TWO));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game_emulator, container, false);

        mBtnWinner[0] = ((Button) rootView.findViewById(R.id.player_one_wins));
        mBtnWinner[1] = ((Button) rootView.findViewById(R.id.player_two_wins));
        Button mBtnDraw = ((Button) rootView.findViewById(R.id.draw));

        mTxtName = new TextView[]{
                (TextView) rootView.findViewById(R.id.playerOneName),
                (TextView) rootView.findViewById(R.id.playerTwoName)};
        mTxtWins = new TextView[]{
                (TextView) rootView.findViewById(R.id.playerOneWins),
                (TextView) rootView.findViewById(R.id.playerTwoWins)};
        mTxtTies = new TextView[]{
                (TextView) rootView.findViewById(R.id.playerOneTies),
                (TextView) rootView.findViewById(R.id.playerTwoTies)};
        mTxtLoss = new TextView[]{
                (TextView) rootView.findViewById(R.id.playerOneLoss),
                (TextView) rootView.findViewById(R.id.playerTwoLoss)};

        mBtnWinner[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                victory(mContestant[0], mContestant[1]);
            }
        });

        mBtnWinner[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                victory(mContestant[1], mContestant[0]);
            }
        });

        mBtnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContestant[0].addTie();
                mContestant[1].addTie();
            }
        });

        setViewValues();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_STATE_SAVED, true);
        outState.putSerializable(ARG_PLAYER_ONE, mContestant[0]);
        outState.putSerializable(ARG_PLAYER_TWO, mContestant[1]);
    }

    private void setViewValues() {
        try {
            String playerWinsString = getContext().getString(R.string.game_emulator_player_wins);
            mBtnWinner[0].setText(String.format(playerWinsString, mContestant[0].getName()));
            mBtnWinner[1].setText(String.format(playerWinsString, mContestant[1].getName()));

            for (int i = 0; i < mContestant.length; i++) {
                mTxtName[i].setText(mContestant[i].getName());
                mTxtWins[i].setText(Integer.toString(mContestant[i].getWins()));
                mTxtTies[i].setText(Integer.toString(mContestant[i].getTies()));
                mTxtLoss[i].setText(Integer.toString(mContestant[i].getLosses()));
            }
        } catch (Exception e) {
            android.util.Log.e(getClass().getName(), "Failed to set fragment values", e);
        }
    }

    private void victory(Contestant winner, Contestant looser) {
        winner.addWin();
        looser.addLoss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Contestant.addListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Contestant.removeListener(this);
    }

    @Override
    public void notifyChange(List<Contestant> contestants) {
        setViewValues();
    }
}
