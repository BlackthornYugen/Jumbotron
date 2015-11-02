package com.steelcomputers.android.assignmenttwo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by black_000 on 2015-10-26.
 */
@ParseClassName("Player")
public class Player extends ParseObject implements java.io.Serializable {

    public Player() {
        // Empty Constructor
    }

    public static class COLUMN {
        public static final String NAME   = "name";
        public static final String WINS   = "wins";
        public static final String LOSSES = "losses";
        public static final String TIES   = "ties";
    }

    private static List<Player> mPlayers           = new ArrayList<Player>();
    private static List<PlayerListener> mListeners = new ArrayList<PlayerListener>();
    private static boolean mIsQueryRunning         = false;
    private String mID;

    public static List<Player> getPlayers() {
        if(!mIsQueryRunning && mPlayers.size() == 0) {
            // If a query isn't running & no players are found, we better check localdb
            queryPlayers(false);
        }
        return mPlayers;
    }
    public static boolean addListener(PlayerListener listener) {
        if (mListeners == null) {
            mListeners = new LinkedList<PlayerListener>();
        }
        return mListeners.add(listener);
    }
    public static boolean removeListener(PlayerListener listener) {
        if (mListeners == null) {
            mListeners = new LinkedList<PlayerListener>();
        }
        return mListeners.remove(listener);
    }
    public static void setPlayers(List players) {
        mPlayers = players;
    }
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

    public static void queryPlayers() {
        mIsQueryRunning = true;
        boolean useNetwork = false;
        try {
            useNetwork = Preferences.getSharedPreferences().getBoolean("cloud_sync", useNetwork);
        } catch (Exception e) {
            Log.e("Player", "Couldn't load sync preference.", e);
        }
        queryPlayers(useNetwork);
    }

    public static void queryPlayers(final boolean useNetwork) {
        mIsQueryRunning = true;
        final ParseQuery<Player> query = ParseQuery.getQuery("Player");
        query.orderByAscending(COLUMN.NAME);
        if (!useNetwork) {
            query.fromLocalDatastore();
        }
        try {
            query.findInBackground(new FindCallback<Player>() {
                public void done(List<Player> playerList, ParseException parseException) {
                    try {
                        if (parseException == null) {
                            if(useNetwork) {
                                ParseObject.pinAllInBackground(playerList); // Pin objects from net
                            }
                            setPlayers(playerList);
                            notifyListeners();
                            Log.d("Player", String.format("Found %s Player(s) from %s.", playerList.size(),
                                    useNetwork ? "the internet" : "local storage"));
                        } else {
                            Log.d("Player", "Error: " + parseException.getMessage(), parseException);
                        }
                    } catch (Exception e) {
                        Log.e("Player", "Error: Unable to handle query results", e);
                    } finally {
                        mIsQueryRunning = false;
                    }
                }
            });
        } catch (Exception e) {
            Log.e("Player", "Error: Failed to execute query", e);
        }
    }

    private static void notifyListeners() {
        if(mListeners != null) {
            for (PlayerListener listener:
                    mListeners) {
                try {
                    listener.notifyChange(mPlayers);
                } catch (Exception e) {
                    Log.e("Player", "Failed to notify change on: " + listener, e);
                }
            }
        }
    }

    public interface PlayerListener {
        void notifyChange(List<Player> players);
    }

    public static class ListAdapter extends ArrayAdapter<Player>
    {
        private int mResource = android.R.layout.simple_list_item_activated_1;
        private int mTextViewResourceId = android.R.id.text1;

        public ListAdapter(Context context, int resource, int textViewResourceId, List<Player> objects) {
            super(context, resource, textViewResourceId, objects);
            mResource = resource;
            mTextViewResourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(mResource, null);
            }

            Player p = getItem(position);

            if (p != null) {
                TextView tt1 = (TextView) v.findViewById(mTextViewResourceId);

                if (tt1 != null) {
                    tt1.setText(p.getName());
                }
            }

            return v;
        }
    }
}
