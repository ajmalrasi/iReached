package com.arscast.ireached;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.arscast.ireached.constants.MapContracts;

/**
 * Created by Rasi on 22-09-2017.
 *
 * Basic database class for the application.
 *
 * The only class that should use this is {@link AppProvider}.
 */

class AppDatabase extends SQLiteOpenHelper{
    private static final String TAG = "AppDatabase";

    public static final String DATABASE_NAME = "iReached.db";
    public static final int DATABASE_VERSION = 1;

    //Implement AppDatabase as a Singleton.
    private static AppDatabase instance = null;

    private AppDatabase(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "AppDatabase: constructor");
    }

    /**
     *
     * Get an instance of the app's singleton database helper object.
     *
     * @param context the content provider context.
     * @return a SQLite database helper object
     */
    static AppDatabase getInstance(Context context){
        if(instance == null){
            Log.d(TAG, "getInstance: Creating new Instance");
            instance = new AppDatabase(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate: starts");
        String sSQL;    //Use a string variable to facilitate logging
        sSQL = "CREATE TABLE "+ MapContracts.TABLE_NAME+ " ("
            + MapContracts.Columns._ID + " INTEGER PRIMARY KEY NOT NULL, "
            + MapContracts.Columns.ALARM_NAME + " TEXT NOT NULL, "
            + MapContracts.Columns.LOC_LATITUDE + " REAL NOT NULL, "
            + MapContracts.Columns.LOC_LONGITUDE + " REAL NOT NULL, "
            + MapContracts.Columns.LOC_RADIUS + " INTEGER NOT NULL, "
            + MapContracts.Columns.ISVIBRATE_ONLY + " INTEGER, "
            + MapContracts.Columns.ISACTIVE + " INTEGER NOT NULL);"; //Database structure
        Log.d(TAG, sSQL);
        sqLiteDatabase.execSQL(sSQL);

        Log.d(TAG, "onCreate: ends");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(TAG, "onUpgrade: starts");
        switch (i){
            case 1:
                //upgrade logic from version 1
                break;
            default:
                throw  new IllegalStateException("onUpgrade with unknown newVersion" + i1);
        }
        Log.d(TAG, "onUpgrade: ends");
    }
}
