package com.steelcomputers.android.assignmenttwo;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.Serializable;

/**
 * A placeholder fragment containing a simple view.
 */
public class GameEmulatorFragment extends Fragment {

    public static final String ARG_PLAYER_ONE = "player_one";
    public static final String ARG_PLAYER_TWO = "player_two";
    public static final String ARG_STATE_SAVED = "state_saved";
    private Player mPlayerOne;
    private Player mPlayerTwo;
    private CollapsingToolbarLayout mAppBar;
    private Button mBtnDraw;
    private Button mBtnWinnerPlayerTwo;
    private Button mBtnWinnerPlayerOne;

    public GameEmulatorFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( savedInstanceState != null && savedInstanceState.getBoolean(ARG_STATE_SAVED)) {
            mPlayerOne = (Player) savedInstanceState.getSerializable(ARG_PLAYER_ONE);
            mPlayerTwo = (Player) savedInstanceState.getSerializable(ARG_PLAYER_TWO);
        } else {
            if (getArguments().containsKey(ARG_PLAYER_ONE)) {
                mPlayerOne = Player.getPlayers().get(getArguments().getInt(ARG_PLAYER_ONE));
            }
            if (getArguments().containsKey(ARG_PLAYER_TWO)) {
                mPlayerTwo = Player.getPlayers().get(getArguments().getInt(ARG_PLAYER_TWO));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game_emulator, container, false);


        mAppBar = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        mBtnWinnerPlayerOne = ((Button) rootView.findViewById(R.id.player_one_wins));
        mBtnWinnerPlayerTwo = ((Button) rootView.findViewById(R.id.player_two_wins));
        mBtnDraw = ((Button) rootView.findViewById(R.id.draw));

        mBtnWinnerPlayerOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                victory(mPlayerOne, mPlayerTwo);
            }
        });

        mBtnWinnerPlayerTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                victory(mPlayerTwo, mPlayerOne);
            }
        });

        mBtnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerOne.addTie();
                mPlayerTwo.addTie();
            }
        });

        setViewValues();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_STATE_SAVED, true);
        outState.putSerializable(ARG_PLAYER_ONE, mPlayerOne);
        outState.putSerializable(ARG_PLAYER_TWO, mPlayerTwo);
    }

    private void setViewValues() {
        try {
            String playerWinsString = getContext().getString(R.string.game_emulator_player_wins);
            mBtnWinnerPlayerOne.setText(String.format(playerWinsString, mPlayerOne.getName()));
            mBtnWinnerPlayerTwo.setText(String.format(playerWinsString, mPlayerTwo.getName()));
        } catch (Exception e) {
            android.util.Log.e(getClass().getName(), "Failed to set fragment values", e);
        }
    }

    private void victory(Player winner, Player looser) {
        winner.addWin();
        looser.addLoss();
    }
}
