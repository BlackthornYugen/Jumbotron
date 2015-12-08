package com.steelcomputers.android.assignmenttwo;

import com.parse.ParseObject;

import java.sql.Time;

/**
 * Created by Manuel on 12/7/2015.
 */
public class Point extends ParseObject
{
    Game currentGame;
    Contestant contestant;
    Time timeStamp;

    public static class COLUMN {
        public static final String GAME = "game";
        public static final String CONTESTANT  = "contestant";
        public static final String TIME = "time";
    }

    public Contestant getContestant() {return (Contestant)getParseObject(COLUMN.CONTESTANT);};
    public void setContestant(Contestant contestant) {put(COLUMN.CONTESTANT, contestant);};
}
