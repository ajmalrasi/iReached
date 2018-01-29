package com.arscast.ireached;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.arscast.ireached.constants.MapContracts;
import com.arscast.ireached.events.OnStartDragListener;
import com.arscast.ireached.events.SimpleItemTouchHelperCallback;
import com.arscast.ireached.services.LocationUpdateService;
import com.arscast.ireached.views.RecyclerAdapter;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.security.InvalidParameterException;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        OnStartDragListener {


    private static final int REQUEST_CODE_LOCATION = 1;
    private boolean PERMISSION_GRANDED = false;
    private static final String TAG = "MainActivity";
    public static final int LOADER_ID = 0;
    private RecyclerAdapter mAdapter; // add adapter reference
    private ItemTouchHelper mItemTouchHelper;

    /**
     * Request code passed to the PlacePicker intent to identify its result when it returns.
     */
    private static final int REQUEST_PLACE_PICKER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent i = new Intent(getApplicationContext(), LocationUpdateService.class);
        // potentially add data to the intent
        i.putExtra("KEY1", "Value to be used by the service");



        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        Intent alarmIntent = getIntent();
        if (alarmIntent.hasExtra("StopAlarm")){
            if(alarmIntent.getBooleanExtra("StopAlarm",false)){
                Log.e(TAG, "onCreate: intent received from service");
                stopService(i);
            }

        }else{
            if (PERMISSION_GRANDED)
                getApplicationContext().startService(i);
            else
                Snackbar.make(findViewById(R.id.mainView),"Please Grand Permission for this app to work",Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();

        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,"Loading...",Snackbar.LENGTH_LONG)
                        .setAction("Action",null).show();
                startPlacePicker();
            }
        });


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RecyclerAdapter(null,this);
        recyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(LOADER_ID,null,this);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    fab.hide();
                else if (dy < 0)
                    fab.show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menumain_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE_LOCATION:{
                //if request cancelled
                if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    PERMISSION_GRANDED=true;

                }else{
                    //permission denied
                    PERMISSION_GRANDED=false;

                }
            }
        }
    }

    public void startPlacePicker(){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the Intent by requesting a result, identified by a request code.
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

            // Hide the pick option in the UI to prevent users from starting the picker
            // multiple times.
//            showPickAction(false);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: starts with request code " + requestCode);
        if (requestCode == REQUEST_PLACE_PICKER) {
            // This result is from the PlacePicker dialog.

            // Enable the picker option
//            showPickAction(true);

            if (resultCode == Activity.RESULT_OK) {
                final Place place = PlacePicker.getPlace(data, this);
                onLocationReceived(place);

                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();

                Log.d(TAG, "onActivityResult: " + name);
            }else{
                Log.d(TAG, "onActivityResult: result code " + resultCode);
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void onLocationReceived(final Place place) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_dialog, null);
        dialogBuilder.setView(dialogView);

        final TextView alarmName = dialogView.findViewById(R.id.dialog_name);
        final TextView alarmCoordinates = dialogView.findViewById(R.id.dialog_coordinates);
        final EditText alarmRadius = dialogView.findViewById(R.id.dialog_radius);
        final CheckBox alarmVibrateOnly = dialogView.findViewById(R.id.dialog_vibrateonly);

        alarmCoordinates.setText(place.getLatLng().toString());
        alarmName.setText(place.getName());
        final LatLng latLng = place.getLatLng();


        dialogBuilder.setTitle("Set Alarm");
//        dialogBuilder.setMessage("Enter text below");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                ContentResolver contentResolver =  getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MapContracts.Columns.ALARM_NAME,place.getName().toString());
                values.put(MapContracts.Columns.LOC_LONGITUDE,latLng.longitude);
                values.put(MapContracts.Columns.LOC_LATITUDE,latLng.latitude);
                values.put(MapContracts.Columns.LOC_RADIUS,alarmRadius.getText().toString());
                values.put(MapContracts.Columns.ISVIBRATE_ONLY,alarmVibrateOnly.isChecked());
                values.put(MapContracts.Columns.ISACTIVE,1);
                Uri uri = contentResolver.insert(MapContracts.CONTENT_URI,values);

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: starts with id " + id);
        String[] projection = {MapContracts.Columns._ID, MapContracts.Columns.ALARM_NAME,
                MapContracts.Columns.LOC_LONGITUDE, MapContracts.Columns.LOC_LATITUDE,MapContracts.Columns.LOC_RADIUS,
                MapContracts.Columns.ISVIBRATE_ONLY,MapContracts.Columns.ISACTIVE};
        // <order by> Tasks.SortOrder, Tasks.Name COLLATE NOCASE
        String sortOrder = MapContracts.Columns._ID + "," + MapContracts.Columns.ALARM_NAME + " COLLATE NOCASE";

        switch(id) {
            case LOADER_ID:
                return new CursorLoader(this,
                        MapContracts.CONTENT_URI,
                        projection,
                        null,
                        null,
                        sortOrder);
            default:
                throw new InvalidParameterException(TAG + ".onCreateLoader called with invalid loader id" + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "Entering onLoadFinished");
        mAdapter.swapCursor(data);
        int count = mAdapter.getItemCount();

        Log.d(TAG, "onLoadFinished: count is " + count);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: starts");
        mAdapter.swapCursor(null);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
