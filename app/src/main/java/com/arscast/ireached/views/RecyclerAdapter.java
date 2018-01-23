package com.arscast.ireached.views;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.arscast.ireached.R;
import com.arscast.ireached.constants.MapContracts;
import com.arscast.ireached.entities.MapItems;
import com.arscast.ireached.events.ItemTouchHelperAdapter;
import com.arscast.ireached.events.ItemTouchHelperViewHolder;
import com.arscast.ireached.events.OnStartDragListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rasi on 16-09-2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> implements ItemTouchHelperAdapter{

    private static final String TAG = "RecyclerAdapter";
    private Cursor cursor;
    private final OnStartDragListener mDragStartListener;
    ContentResolver resolver;


    public RecyclerAdapter(Cursor cursor,OnStartDragListener dragStartListener) {
        Log.d(TAG, "RecyclerAdapter: Constructor called");
        this.cursor = cursor;
        this.mDragStartListener = dragStartListener;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: new view requested");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_alarm_item,parent,false);
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        resolver = view.getContext().getContentResolver();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {

        if ((cursor == null) || (cursor.getCount()==0)){
            Log.d(TAG, "onBindViewHolder: providing instructions");
            holder.alarmTitle.setText(R.string.instruction_heading);
            holder.alarmLocation.setText(R.string.instructions);
            holder.alarmRadius.setVisibility(View.GONE);
            holder.alarmIsActive.setVisibility(View.GONE);
        }else{
            if (!cursor.moveToPosition(position)){
                throw new IllegalStateException("Couldn't move cursor to position " + position);
            }

            final MapItems items = new MapItems(cursor.getLong(cursor.getColumnIndex(MapContracts.Columns._ID)),
                    cursor.getString(cursor.getColumnIndex(MapContracts.Columns.ALARM_NAME)),
                    cursor.getDouble(cursor.getColumnIndex(MapContracts.Columns.LOC_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(MapContracts.Columns.LOC_LONGITUDE)),
                    cursor.getInt(cursor.getColumnIndex(MapContracts.Columns.LOC_RADIUS)),
                    cursor.getInt(cursor.getColumnIndex(MapContracts.Columns.ISVIBRATE_ONLY)),
                    cursor.getInt(cursor.getColumnIndex(MapContracts.Columns.ISACTIVE)));

            String radius = holder.resources.getString(R.string.radius,items.getRadius());
            String latlng = holder.resources.getString(R.string.latloc,items.getLatitude(),items.getLongitude());

            holder.alarmTitle.setText(items.getName());
            holder.alarmLocation.setText(Html.fromHtml(latlng));
            holder.alarmRadius.setText(radius);
            if(items.isActive() == 0){
                holder.alarmIsActive.setChecked(false);
            }else{
                holder.alarmIsActive.setChecked(true);
            }
            if (items.isVibrateOnly() == 0){
                holder.alarmType.setImageResource(R.drawable.ic_access_alarm_black_24dp);
            }else{
                holder.alarmType.setImageResource(R.drawable.ic_vibration_black_24dp);
            }

            holder.alarmIsActive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ContentResolver contentResolver = view.getContext().getContentResolver();
                    ContentValues values = new ContentValues();
                    if(((Switch)view).isChecked()){
                        Log.e(TAG, "onClick: "+ items.getId() +". " +items.getName()+ " is true");
                        values.put(MapContracts.Columns.ISACTIVE, 1);
                        contentResolver.update(MapContracts.buildTaskUri(items.getId()),values,null,null);
                    }else {
                        Log.e(TAG, "onClick: "+ items.getId() +". " +items.getName()+" is false");
                        values.put(MapContracts.Columns.ISACTIVE, 0);
                        contentResolver.update(MapContracts.buildTaskUri(items.getId()),values,null,null);
                    }
                }
            });
        }
    }


    @Override
    public int getItemCount() {

        if ((cursor == null) || (cursor.getCount()==0)){
            return 1;
        }else{
            return cursor.getCount();
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        Log.d(TAG, "onItemDismiss: "+ position + " removed");
        if (true){
            Log.d(TAG, "onItemDismiss: Null");
        }else{
            Log.d(TAG, "onItemDismiss: ");
            if (resolver!=null){
//                resolver.delete(MapContracts.buildTaskUri(items.getId()),null,null);

            }
        }
        notifyItemRemoved(position);

    }


    public interface MapListener{
        void onClicked(MapItems mapItems);
    }


    /**
     * Swap a new Cursor, returning the old Cursor.
     * The returned old Cursor is <em>not</em> closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one.
     * If the given new Cursor is the same instance as the previously set
     * Cursor, null is also returned
     */
    public Cursor swapCursor(Cursor newCursor){
        if (newCursor == cursor){
            return null;
        }

        final Cursor oldCursor = cursor;
        cursor = newCursor;
        if (newCursor != null){
            //notify the observers about the new cursor.
            notifyDataSetChanged();
        }else{
            //notify the observer about the lack of a data set.
            notifyItemRangeRemoved(0,getItemCount());
        }
        return oldCursor;
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        private static final String TAG = "RecyclerViewHolder";

        TextView alarmTitle;
        TextView alarmLocation;
        TextView alarmRadius;
        Switch alarmIsActive;
        ImageView alarmType;
        Resources resources;


        public RecyclerViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "RecyclerViewHolder: starts");

            this.alarmTitle = itemView.findViewById(R.id.listText);
            this.alarmLocation = itemView.findViewById(R.id.listLocation);
            this.alarmRadius = itemView.findViewById(R.id.listRadius);
            this.alarmIsActive = itemView.findViewById(R.id.list_switchIsActive);
            this.alarmType = itemView.findViewById(R.id.alarmStatus);
            this.resources  = itemView.getResources();
        }

        @Override
        public void onItemSelected() {
//            itemView.setBackgroundColor(Color.argb(100,255,255,255));
        }

        @Override
        public void onItemClear() {
//            itemView.setBackgroundColor(Color.argb(255,0,255,0));
        }
    }

}
