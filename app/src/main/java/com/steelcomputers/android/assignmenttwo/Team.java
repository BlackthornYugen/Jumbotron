package com.steelcomputers.android.assignmenttwo;

import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 11/30/2015.
 */
public class Team extends ParseObject implements java.io.Serializable
{
    public Team(){
        // Empty Constructor
    }

    public void addLoss() {
        setLosses(getLosses() + 1);
        doSave();
    }

    public void addWin() {
        setWins(getWins() + 1);
        doSave();
    }

    public void addTie() {
        setTies(getTies() + 1);
        doSave();
    }

    public static class COLUMN {
        public static final String NAME   = "name";
        public static final String WINS   = "wins";
        public static final String LOSSES = "losses";
        public static final String TIES   = "ties";
    }

    private static List<Player> mPlayers           = new ArrayList<Player>();
    //private static List<PlayerListener> mListeners = new ArrayList<PlayerListener>();
    private static boolean mIsQueryRunning         = false;
    private String mID;

    public static boolean isRunningAQuery() {
        return mIsQueryRunning;
    }
    public String getId() {
        return mID;
    }
    public void setId(String id) {
        mID = id;
    }
    public String getName() {
        return getString(COLUMN.NAME);
    }
    public void setName(String name) {
        put(COLUMN.NAME, name);
    }
    public int getWins() {
        return getInt(COLUMN.WINS);
    }
    public void setWins(int wins) {
        put(COLUMN.WINS, wins);
    }
    public int getLosses() {
        return getInt(COLUMN.LOSSES);
    }
    public void setLosses(int losses) {
        put(COLUMN.LOSSES, losses);
    }
    public int getTies() {
        return getInt(COLUMN.TIES);
    }
    public void setTies(int ties) {
        put(COLUMN.TIES, ties);
    }




    private void doSave() {
        try {
            this.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException parseException) {
                    try {
                        if (parseException == null) {
                            Player.queryPlayers(false); // Update players
                        } else {
                            throw parseException;
                        }
                    } catch (ParseException e) {
                        Log.e(this.getClass().getName(), "Cannot save locally", e);
                    }
                }
            });
            if (Preferences.getSharedPreferences().getBoolean("cloud_sync", false)) {
                this.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException parseException) {
                        if (parseException != null) {
                            Log.e(this.getClass().getName(), "Cannot save remotely.", parseException);
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Couldn't create player.", e);
        }
    }

    private void doDelete() {
        unpinInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException parseException) {
                try {
                    if (parseException == null) {
                        Player.queryPlayers(false); // Update players
                    } else {
                        throw parseException;
                    }
                } catch (ParseException e) {
                    Log.e("Player", "Player could not be deleted locally", e);
                }
            }
        });

        if (Preferences.getSharedPreferences().getBoolean("cloud_sync", false)) {
            deleteEventually(new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e("Player", "Player could not be deleted remotely", e);
                    }
                }
            });
        }
    }

}
