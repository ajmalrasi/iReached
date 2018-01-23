package com.arscast.ireached.constants;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.arscast.ireached.AppProvider.CONTENT_AUTHORITY;
import static com.arscast.ireached.AppProvider.CONTENT_AUTHORITY_URI;

/**
 * Created by Rasi on 20-09-2017.
 */

public class MapContracts {

    public static final String TABLE_NAME = "Alarms";

    public static class Columns{
        public static final String _ID = BaseColumns._ID;
        public static final String ALARM_NAME = "Name";
        public static final String LOC_LATITUDE = "Latitude";
        public static final String LOC_LONGITUDE = "Longitude";
        public static final String LOC_RADIUS = "Radius";
        public static final String ISACTIVE = "IsActive";
        public static final String ISVIBRATE_ONLY = "IsVibrateOnly";

        private Columns(){
            //Private constructor to prevent instantiation
        }
    }

    public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI,TABLE_NAME);

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;

    public static  Uri buildTaskUri(long taskId){
        return ContentUris.withAppendedId(CONTENT_URI,taskId);
    }

    public static  long getTaskId(Uri uri){
        return ContentUris.parseId(uri);
    }
}
