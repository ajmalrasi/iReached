package com.arscast.ireached;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.arscast.ireached.constants.MapContracts;

/**
 * Created by Rasi on 22-09-2017.
 *
 * Provider for the iReached app. This is the only class that knows about {@link AppDatabase}
 */

public class AppProvider extends ContentProvider {
    private static final String TAG = "AppProvider";

    private AppDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static final String CONTENT_AUTHORITY = "com.arscast.ireached.provider";
    public static final Uri CONTENT_AUTHORITY_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final int TASKS = 100;
    private static final int TASKS_ID = 101;

    /*
      private static final int TASK_TIMINGS = 400;
      private static final int TASK_TIMINGS_ID = 401;
     */

    private static final int TASK_DURATIONS =400;
    private static final int TASK_DURATIONS_ID = 401;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);

        // eg. content://com.arscast.ireached.provider/Tasks
        matcher.addURI(CONTENT_AUTHORITY, MapContracts.TABLE_NAME,TASKS);
        // eg. content://com.arscast.ireached.provider/Tasks/8
        matcher.addURI(CONTENT_AUTHORITY,MapContracts.TABLE_NAME + "/#",TASKS_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper =  AppDatabase.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        Log.d(TAG, "query: called with URL "+ uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "query: match is " + match);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (match){
            case TASKS:
                queryBuilder.setTables(MapContracts.TABLE_NAME);
                break;

            case TASKS_ID:
                queryBuilder.setTables(MapContracts.TABLE_NAME);
                long taskId = MapContracts.getTaskId(uri);
                queryBuilder.appendWhere(MapContracts.Columns._ID + " = " + taskId);
                break;

            default:
                throw  new IllegalArgumentException("Unknown URI: "+ uri);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return queryBuilder.query(db,strings,s,strings1,null,null,s1);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case TASKS:
                return MapContracts.CONTENT_TYPE;

            case TASKS_ID:
                return MapContracts.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        Log.d(TAG, "Entering insert, called with uri: " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "match is "+ match);

        final SQLiteDatabase db;
        Uri returnUri;
        long recordId;
        switch (match){
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                recordId =  db.insert(MapContracts.TABLE_NAME,null,contentValues);
                if (recordId>=0){
                    returnUri = MapContracts.buildTaskUri(recordId);
                } else {
                    throw new android.database.SQLException("Failed to insert into " + uri.toString());
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown uri " + uri);
        }
        Log.d(TAG, "Existing insert, returning" + returnUri);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        Log.d(TAG, "delete: called with  " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "match is "+match);

        final SQLiteDatabase db;
        int count;

        String selectionCriteria;

        switch(match){
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                count = db.delete(MapContracts.TABLE_NAME,s,strings);
                break;

            case TASKS_ID:
                db = mOpenHelper.getWritableDatabase();
                long taskId = MapContracts.getTaskId(uri);
                selectionCriteria = MapContracts.Columns._ID + " = " + taskId;

                if ((s != null) && (s.length()>0)){
                    selectionCriteria += " AND (" + s + ")";
                }
                count = db.delete(MapContracts.TABLE_NAME,selectionCriteria,strings);
                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        Log.d(TAG, "Exiting delete, returning " + count);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        Log.d(TAG, "update: called with uri " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "match is "+match);

        final SQLiteDatabase db;
        int count;

        String selectionCriteria;

        switch(match){
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                count = db.update(MapContracts.TABLE_NAME,contentValues,s,strings);
                break;

            case TASKS_ID:
                db = mOpenHelper.getWritableDatabase();
                long taskId = MapContracts.getTaskId(uri);
                selectionCriteria = MapContracts.Columns._ID + " = " + taskId;

                if ((s != null) && (s.length()>0)){
                    selectionCriteria += " AND (" + s + ")";
                }
                count = db.update(MapContracts.TABLE_NAME,contentValues,selectionCriteria,strings);
                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        Log.d(TAG, "Exiting update, returning " + count);
        return count;
    }
}
