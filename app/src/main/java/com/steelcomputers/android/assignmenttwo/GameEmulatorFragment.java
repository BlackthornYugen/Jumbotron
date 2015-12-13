package com.steelcomputers.android.assignmenttwo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.media.MediaRouter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.steelcomputers.android.assignmenttwo.CastScoreService.ScoreBinder;

import java.util.List;

/**
 * GameEmulatorFragment.java
 *
 * A placeholder fragment containing a simple view.
 *
 * Created by John Steel on 2015-11-02 from a template.
 * Modified by Manuel Lopez on 2015-12-08
 */
public class GameEmulatorFragment extends Fragment implements Contestant.PlayerListener {

    public static final String ARG_PLAYER_ONE = "player_one";
    public static final String ARG_PLAYER_TWO = "player_two";
    public static final String ARG_STATE_SAVED = "state_saved";
    private Contestant[] mContestant = new Contestant[2];
    private Button[] mBtnScore = new Button[2];
    private Button[] mBtnMinus = new Button[2];

    private Button mBtnResetGame;
    private Button mBtnStartFromScratch;
    private ScoreBinder mCastService;

    private TextView[] mTxtName;
    private TextView[] mTxtPoints;

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

        mBtnScore[0] = ((Button) rootView.findViewById(R.id.player_one_score));
        mBtnScore[1] = ((Button) rootView.findViewById(R.id.player_two_score));

        mBtnMinus[0] = ((Button) rootView.findViewById(R.id.player_one_minus));
        mBtnMinus[1] = ((Button) rootView.findViewById(R.id.player_two_minus));


        mBtnResetGame = ((Button) rootView.findViewById(R.id.reset_game));
        mBtnStartFromScratch = ((Button) rootView.findViewById(R.id.start_scratch));

        mTxtName = new TextView[]{
                (TextView) rootView.findViewById(R.id.playerOneName),
                (TextView) rootView.findViewById(R.id.playerTwoName)};
        mTxtPoints = new TextView[]{
                (TextView) rootView.findViewById(R.id.playerOnePoints),
                (TextView) rootView.findViewById(R.id.playerTwoPoints)};

        mBtnScore[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                victory(mContestant[0], mContestant[1]);
            }
        });

        mBtnScore[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                victory(mContestant[1], mContestant[0]);
            }
        });


        mBtnMinus[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minus(mContestant[0], mContestant[1]);
            }
        });

        mBtnMinus[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minus(mContestant[1], mContestant[0]);
            }
        });


        mBtnResetGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWinLosAndDraw(mContestant[1], mContestant[0]);

                mContestant[1].resetGame(mContestant[0]);
                mContestant[0].resetGame(mContestant[1]);

                setViewValues();
            }
        });

        mBtnStartFromScratch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContestant[1].resetGame(mContestant[0]);
                mContestant[0].resetGame(mContestant[1]);

                setViewValues();
            }
        });

        setViewValues();
        return rootView;
    }

    /**
     * Based on their current game points will decided which one
     * was the winner, losses, draw and will added to player
     * @param one
     * @param two
     */
    void addWinLosAndDraw(Contestant one, Contestant two)
    {
        if (Integer.compare(one.getPoints(two), two.getPoints(one)) == 0)
        {
            one.addTie();
            two.addTie();

            Toast.makeText(this.getContext(),
                    this.getResources().getString(R.string.is_a_tie) , Toast.LENGTH_LONG).show();
        }
        else if (Integer.compare(one.getPoints(two), two.getPoints(one)) < 0)
        {
            one.addLoss();
            two.addWin();

            Toast.makeText(this.getContext(), two.getName() + " " +
                    this.getResources().getString(R.string.won) , Toast.LENGTH_LONG).show();
        }
        else if (Integer.compare(one.getPoints(two), two.getPoints(one)) > 0)
        {
            one.addWin();
            two.addLoss();

            Toast.makeText(this.getContext(), one.getName() + " " +
                    this.getResources().getString(R.string.won) , Toast.LENGTH_LONG).show();
        }
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
            String playerWinsString = getContext().getString(R.string.game_emulator_player_score);

            mBtnScore[0].setText(String.format(playerWinsString, mContestant[0].getName()));
            mBtnScore[1].setText(String.format(playerWinsString, mContestant[1].getName()));

            String playerMinusString = getContext().getString(R.string.game_emulator_player_minus);

            mBtnMinus[0].setText(String.format(playerMinusString, mContestant[0].getName()));
            mBtnMinus[1].setText(String.format(playerMinusString, mContestant[1].getName()));

            for (int i = 0; i < mContestant.length; i++) {
                mTxtName[i].setText(mContestant[i].getName());
            }

            mTxtPoints[0].setText(Integer.toString(mContestant[0].getPoints(mContestant[1])));
            mTxtPoints[1].setText(Integer.toString(mContestant[1].getPoints(mContestant[0])));

        } catch (Exception e) {
            android.util.Log.e(getClass().getName(), "Failed to set fragment values", e);
        }
    }

    private void victory(Contestant winner, Contestant looser) {
        winner.addPoint(looser);
    }


    private void minus(Contestant winner, Contestant looser) {
        winner.minusPoint(looser);
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
