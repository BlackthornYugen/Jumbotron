package com.steelcomputers.android.assignmenttwo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Contestant.java
 *
 * Extends {@link ParseObject} to facilitate local and remote storage. Dialog
 * factories have been made to make renaming, deleting and creating players as
 * easy as possible to do without having knowledge of Parse's library.
 *
 * Created by John Steel on 2015-10-26.
 * Modified by Manuel Lopez on 2015-12-08
 */
@ParseClassName("Contestant")
public class Contestant extends ParseObject implements java.io.Serializable {

    boolean isATeam;//will indicate if is a team the contestant

    public Contestant() {
        // Empty Constructor
    }

    public Contestant(boolean isATeam) {
        this.isATeam = isATeam;
    }

    /**
     * The action of adding a point to the contestant
     */
    public void addPoint(Contestant other)
    {
        setPoints(getPoints(other) + 1, other);
        doSave();
    }

    /**
     * The action of reseting a game
     * @param other The specific game will be reseted
     */
    public void resetGame(Contestant other)
    {
        setPoints(0, other);
    }

//    public void addLoss() {
//        setLosses(getLosses() + 1);
//        doSave();
//    }
//
//    public void addWin() {
//        setWins(getWins() + 1);
//        doSave();
//    }
//
//    public void addTie() {
//        setTies(getTies() + 1);
//        doSave();
//    }

    public static class COLUMN {
        public static final String POINTS = "point";
        public static final String NAME   = "name";
        public static final String WINS   = "wins";
        public static final String LOSSES = "losses";
        public static final String TIES   = "ties";
    }

    private static List<Contestant> mContestants = new ArrayList<Contestant>();
    private static List<PlayerListener> mListeners = new ArrayList<PlayerListener>();
    private static boolean mIsQueryRunning         = false;
    private String mID;

    public static List<Contestant> getPlayers() {
        if(!mIsQueryRunning && mContestants.size() == 0) {
            // If a query isn't running & no players are found, we better check localdb
            queryPlayers(false);
        }
        return mContestants;
    }
    public static void addListener(PlayerListener listener) {
        if (mListeners == null) {
            mListeners = new LinkedList<PlayerListener>();
        }
        mListeners.add(listener);
    }
    public static void removeListener(PlayerListener listener) {
        if (mListeners == null) {
            mListeners = new LinkedList<PlayerListener>();
        }
        mListeners.remove(listener);
    }
    public static void setPlayers(List<Contestant> contestants) {
        try {
            mContestants = contestants;
        } catch (Exception e) {
            Log.e(Contestant.class.getName(),"Unable to set contestants list", e);
        }
    }


    public int getPoints(Contestant other) {
        return getInt(COLUMN.POINTS + "." + other.getName());
    }
    public void setPoints(int points, Contestant other) {
        put(COLUMN.POINTS + "." + other.getName(), points);
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
        SharedPreferences preferences = Preferences.getSharedPreferences();
        try {
            useNetwork = preferences.getBoolean("cloud_sync", false);
        } catch (Exception e) {
            Log.e("Contestant", "Couldn't load sync preference.", e);
        }
        queryPlayers(useNetwork);
    }

    public static void queryPlayers(final boolean useNetwork) {
        mIsQueryRunning = true;
        final ParseQuery<Contestant> query = ParseQuery.getQuery("Contestant");
        final SharedPreferences preferences = Preferences.getSharedPreferences();
        query.orderByAscending(COLUMN.NAME);
        if (!useNetwork) {
            query.fromLocalDatastore();
        }
        try {
            query.findInBackground(new FindCallback<Contestant>() {
                public void done(List<Contestant> contestantList, ParseException parseException) {
                    try {
                        if (parseException == null) {
                            if(useNetwork) {
                                if(!preferences.getBoolean("cloud_keep_local", false)) {
                                    ParseObject.unpinAll(getPlayers()); // Remove old players
                                }
                                ParseObject.pinAllInBackground(contestantList); // Pin players from net
                            }
                            setPlayers(contestantList);
                            notifyListeners();
                            Log.d("Contestant", String.format("Found %s Contestant(s) from %s.", contestantList.size(),
                                    useNetwork ? "the internet" : "local storage"));
                        } else {
                            Log.d("Contestant", "Error: " + parseException.getMessage(), parseException);
                        }
                    } catch (Exception e) {
                        Log.e("Contestant", "Error: Unable to handle query results", e);
                    } finally {
                        mIsQueryRunning = false;
                    }
                }
            });
        } catch (Exception e) {
            Log.e("Contestant", "Error: Failed to execute query", e);
        }
    }

    private static void notifyListeners() {
        if(mListeners != null) {
            for (PlayerListener listener:
                    mListeners) {
                try {
                    listener.notifyChange(mContestants);
                } catch (Exception e) {
                    Log.e("Contestant", "Failed to notify change on: " + listener, e);
                }
            }
        }
    }

    public interface PlayerListener {
        void notifyChange(List<Contestant> contestants);
    }

    public static class ListAdapter extends ArrayAdapter<Contestant>
    {
        private int mResource = android.R.layout.simple_list_item_activated_1;
        private int mTextViewResourceId = android.R.id.text1;

        public ListAdapter(Context context, int resource, List<Contestant> objects) {
            super(context, resource, objects);
            mResource = resource;
        }

        public ListAdapter(Context context, int resource, int textViewResourceId, List<Contestant> objects) {
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

            Contestant p = getItem(position);

            if (p != null) {
                TextView tt1 = (TextView) v.findViewById(mTextViewResourceId);

                if (tt1 != null) {
                    tt1.setText(p.getName());
                }
            }

            return v;
        }
    }

    @NonNull
    public AlertDialog.Builder getDeletePlayerDialog(Activity context) {
        final Contestant contestant = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(String.format(context.getString(R.string.player_delete_confirmation), getName()));
        builder.setPositiveButton(R.string.player_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                contestant.doDelete();
            }
        });
        builder.setNegativeButton(R.string.cancel_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder;
    }

    @NonNull
    public static AlertDialog.Builder getNewPlayerDialog(Activity context, boolean isATeam)
    {
        String teamOrPlayer = DefineIfPlayerOrTeam(isATeam);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter " + teamOrPlayer + " Name");
        setCreateOrRenameDialogButtons(builder, context, new Contestant(isATeam), "Add " + teamOrPlayer, "Cancel");
        return builder;
    }

    static String DefineIfPlayerOrTeam(boolean isATeam)
    {
        if (isATeam){
            return "Team";
        }
        return "Player";
    }

    @NonNull
    public AlertDialog.Builder getRenamePlayerDialog(Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(String.format("Rename \"%s\"", getName()));
        setCreateOrRenameDialogButtons(builder, context, this, "Rename", "Don't rename");
        return builder;
    }

    private static void setCreateOrRenameDialogButtons(AlertDialog.Builder builder,
                                                       Activity context,
                                                       final Contestant contestant,
                                                       String positive,
                                                       String negative) {
        // Create the input view
        final EditText input = new EditText(context);
        if (contestant.getName() != null) {
            input.setText(contestant.getName());
            input.selectAll();
        }

        // Rename the enter key on the android keyboard
        input.setImeActionLabel(positive, android.view.KeyEvent.KEYCODE_ENTER);

        // Auto-capitalize first/last names
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        // Set the builder view to our input view
        builder.setView(input);

        // When keyboard KEYCODE_ENTER is activated
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                Log.d(this.getClass().getName(), Integer.toBinaryString(keyCode));
                if (keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                    try {
                        contestant.setName(input.getText().toString().trim());
                        contestant.doSave();
                        dialog.dismiss();
                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), "Couldn't save contestant.", e);
                    }
                    return false;
                }
                return true;
            }
        });

        // When positive button is activated
        builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    contestant.setName(input.getText().toString().trim());
                    contestant.doSave();
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), "Couldn't save contestant.", e);
                }
            }
        });

        // When negative button is activated
        builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    @NonNull
    public AlertDialog.Builder getPlayersDialog(Activity context,
                                                       DialogInterface.OnClickListener listener,
                                                       List<Contestant> contestants) {
        if (contestants == null) {
            contestants = getPlayers();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ListAdapter listAdapter = new ListAdapter(context,
                android.R.layout.simple_list_item_activated_1, contestants);
        builder.setAdapter(listAdapter, listener);
        builder.setTitle("Select an opponent");
        return builder;
    }

    private void doSave() {
        try {
            this.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException parseException) {
                    try {
                        if (parseException == null) {
                            Contestant.queryPlayers(false); // Update players
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
                        Contestant.queryPlayers(false); // Update players
                    } else {
                        throw parseException;
                    }
                } catch (ParseException e) {
                    Log.e("Contestant", "Contestant could not be deleted locally", e);
                }
            }
        });

        if (Preferences.getSharedPreferences().getBoolean("cloud_sync", false)) {
            deleteEventually(new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e("Contestant", "Contestant could not be deleted remotely", e);
                    }
                }
            });
        }
    }
}
