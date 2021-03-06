package com.steelcomputers.android.jumbotron;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Preferences.java
 *
 * Created by John Steel on 2015-11-01.
 */
public class Preferences {
    private static Preferences mInstance;
    private static SharedPreferences mSharedPreferences;
    private static Activity mContext;

    private Preferences() {
        // Default constructor
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static Preferences getInstance() {
        if (mInstance == null) {
            mInstance = new Preferences();
        }
        return mInstance;
    }

    public static SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public static void setContext(Activity activity) {
        mContext = activity;
        getInstance();
    }
}
