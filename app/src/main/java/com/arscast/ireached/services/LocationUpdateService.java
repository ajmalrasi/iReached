package com.arscast.ireached.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.arscast.ireached.MainActivity;
import com.arscast.ireached.R;
import com.arscast.ireached.constants.MapContracts;
import com.arscast.ireached.entities.MapItems;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Rasi on 19-09-2017.
 */

public class LocationUpdateService extends Service {


    private static final String TAG = "LocationUpdateService";
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallBack;
    private int count = 0;
    private MapItems items;
    private ArrayList<MapItems> itemList;
    private boolean isPlaying = false;
    Vibrator v;
    MediaPlayer player;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //create an instance of fused location provider
        Log.d("FROM-SERVICE", "Service onCreate");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("StopAlarm",true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_access_alarm_black_24dp)
                .setContentTitle("iReached")
                .setContentText("Location Service")
                .addAction(R.drawable.ic_notifications_off_black_24dp, "Stop Alarm", pendingIntent)
                .setContentIntent(pendingIntent).build();
        ContentResolver contentResolver = getContentResolver();

        String[] projection = {MapContracts.Columns._ID,
                MapContracts.Columns.ALARM_NAME,
                MapContracts.Columns.LOC_LONGITUDE,
                MapContracts.Columns.LOC_LATITUDE,
                MapContracts.Columns.LOC_RADIUS,
                MapContracts.Columns.ISVIBRATE_ONLY,
                MapContracts.Columns.ISACTIVE};

        Cursor cursor = contentResolver.query(MapContracts.CONTENT_URI,
                projection,
                null,
                null,
                MapContracts.Columns._ID);

        if ((cursor == null) || (cursor.getCount() == 0)) {
            Log.d(TAG, "onCreate: cursor null");
            itemList = new ArrayList<>();
        } else {

            Log.d(TAG, "onCreate: number of rows " + cursor.getCount());
            itemList = new ArrayList<>();
            while (cursor.moveToNext()) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    if (cursor.getInt(cursor.getColumnIndex(MapContracts.Columns.ISACTIVE)) == 1) {
                        items = new MapItems(cursor.getLong(cursor.getColumnIndex(MapContracts.Columns._ID)),
                                cursor.getString(cursor.getColumnIndex(MapContracts.Columns.ALARM_NAME)),
                                cursor.getDouble(cursor.getColumnIndex(MapContracts.Columns.LOC_LATITUDE)),
                                cursor.getDouble(cursor.getColumnIndex(MapContracts.Columns.LOC_LONGITUDE)),
                                cursor.getInt(cursor.getColumnIndex(MapContracts.Columns.LOC_RADIUS)),
                                cursor.getInt(cursor.getColumnIndex(MapContracts.Columns.ISVIBRATE_ONLY)),
                                cursor.getInt(cursor.getColumnIndex(MapContracts.Columns.ISACTIVE)));
                        itemList.add(i, items);
                        Log.d(TAG, "onCreate: " + cursor.getColumnName(i) + ": " + cursor.getString(i));
                    }
                }
                Log.d(TAG, "onCreate: ================================");
            }
            cursor.close();
        }


        startForeground(1337, notification);
        locationClient = LocationServices.getFusedLocationProviderClient(getBaseContext());
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    for (int j = 0; j < itemList.size(); j++) {
                        Location loc = new Location("List");
                        loc.setLongitude(itemList.get(j).getLongitude());
                        loc.setLatitude(itemList.get(j).getLatitude());
                        float distanceInMeters = location.distanceTo(loc);
                        boolean isWithin1km = distanceInMeters < itemList.get(j).getRadius();
                        if (isWithin1km && !isPlaying) {
                            isPlaying = true;
                            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            long[] pattern = {0, 400, 300,400,300,500,2000,400, 300,400,300,500};
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                            player = MediaPlayer.create(getApplicationContext(),notification);
                            v.vibrate(pattern,6);
                            player.setLooping(true);
                            if(itemList.get(j).isVibrateOnly() == 1){
                                v.vibrate(pattern,6);
                            }else{
                                v.vibrate(pattern,6);
                                player.start();
                            }
                            Log.d(TAG, "onLocationResult: " + itemList.get(j).getName());
                        }
                    }
                    count++;
                    if (count == 10) {
                        stopSelf();
                    }
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Runnable r;
        r = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                startLocationUpdates();
                Log.d("FROM-SERVICE", "Service started");
                Looper.loop();
            }
        };
        Thread testThread = new Thread(r);
        testThread.setPriority(10);
        testThread.start();
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.d("FROM-SERVICE", "Service Destroyed");
        stopLocationUpdates();
        if (player != null){
            player.stop();
        }
        if(v != null){
            v.cancel();
        }
    }


    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        return mLocationRequest;
    }

    private void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: stopped");
        locationClient.removeLocationUpdates(locationCallBack);
    }


    private void startLocationUpdates() {
        try {
            locationClient.requestLocationUpdates(createLocationRequest(),
                    locationCallBack, Looper.myLooper());
        } catch (NullPointerException e) {
            Log.d("FROM-SERVICE", "Null");
        }
    }




}
