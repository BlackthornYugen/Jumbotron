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
public class GameEmulatorFragment extends Fragment implements Player.PlayerListener {

    public static final String ARG_PLAYER_ONE = "player_one";
    public static final String ARG_PLAYER_TWO = "player_two";
    public static final String ARG_STATE_SAVED = "state_saved";
    private Player[] mPlayer = new Player[2];
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
            mPlayer[0] = (Player) savedInstanceState.getSerializable(ARG_PLAYER_ONE);
            mPlayer[1] = (Player) savedInstanceState.getSerializable(ARG_PLAYER_TWO);
        } else {
            if (getArguments().containsKey(ARG_PLAYER_ONE)) {
                mPlayer[0] = Player.getPlayers().get(getArguments().getInt(ARG_PLAYER_ONE));
            }
            if (getArguments().containsKey(ARG_PLAYER_TWO)) {
                mPlayer[1] = Player.getPlayers().get(getArguments().getInt(ARG_PLAYER_TWO));
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
                victory(mPlayer[0], mPlayer[1]);
            }
        });

        mBtnWinner[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                victory(mPlayer[1], mPlayer[0]);
            }
        });

        mBtnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer[0].addTie();
                mPlayer[1].addTie();
            }
        });

        setViewValues();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_STATE_SAVED, true);
        outState.putSerializable(ARG_PLAYER_ONE, mPlayer[0]);
        outState.putSerializable(ARG_PLAYER_TWO, mPlayer[1]);
    }

    private void setViewValues() {
        try {
            String playerWinsString = getContext().getString(R.string.game_emulator_player_wins);
            mBtnWinner[0].setText(String.format(playerWinsString, mPlayer[0].getName()));
            mBtnWinner[1].setText(String.format(playerWinsString, mPlayer[1].getName()));

            for (int i = 0; i < mPlayer.length; i++) {
                mTxtName[i].setText(mPlayer[i].getName());
                mTxtWins[i].setText(Integer.toString(mPlayer[i].getWins()));
                mTxtTies[i].setText(Integer.toString(mPlayer[i].getTies()));
                mTxtLoss[i].setText(Integer.toString(mPlayer[i].getLosses()));
            }
        } catch (Exception e) {
            android.util.Log.e(getClass().getName(), "Failed to set fragment values", e);
        }
    }

    private void victory(Player winner, Player looser) {
        winner.addWin();
        looser.addLoss();
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

    @Override
    public void notifyChange(List<Player> players) {
        setViewValues();
    }
}
