package com.arscast.ireached.entities;


import java.io.Serializable;

/**
 * Created by Rasi on 15-09-2017.
 */

public class MapItems implements Serializable {

    public static final long serialVersionUID = 20170922L;

    private long m_Id;
    private final String mName;
    private final double mLatitude;
    private final double mLongitude;
    private final int mRadius;
    private final int mIsVibrateOnly;
    private final int mIsActive;


    public MapItems(long id, String mName, double mLatitude, double mLongitude, int mRadius, int mIsVibrateOnly, int mIsActive) {
        this.m_Id = id;
        this.mName = mName;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mRadius = mRadius;
        this.mIsVibrateOnly = mIsVibrateOnly;
        this.mIsActive = mIsActive;
    }

    public long getId() {
        return m_Id;
    }

    public String getName() {
        return mName;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public int getRadius() {
        return mRadius;
    }

    public int isVibrateOnly() {
        return mIsVibrateOnly;
    }

    public int isActive() {
        return mIsActive;
    }

    public void setId(long id) {
        this.m_Id = id;
    }


    @Override
    public String toString() {
        return "MapItems{" +
                "m_Id=" + m_Id +
                ", mName='" + mName + '\'' +
                ", mLatitude=" + mLatitude +
                ", mLongitude=" + mLongitude +
                ", mRadius=" + mRadius +
                ", mIsVibrateOnly=" + mIsVibrateOnly +
                ", mIsActive=" + mIsActive +
                '}';
    }
}
