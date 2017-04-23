package com.example.flex.gpsalarm;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.List;

/**
 * Created by Flex on 4/15/2017.
 */

public class DestinationHeader implements Parent<DestinationOptions> {
    private List<DestinationOptions> mOptions;
    private String mDestinationAddress;
    private boolean mIsSwitchChecked;
    private double mLatitude;
    private double mLongitude;

    public DestinationHeader(String destinationAddress, boolean isChecked, List<DestinationOptions> options) {
        mOptions = options;
        mDestinationAddress = destinationAddress;
        mIsSwitchChecked = isChecked;
    }

    @Override
    public List<DestinationOptions> getChildList() {
        return mOptions;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    public String getDestinationAddress() {
        return mDestinationAddress;
    }

    public double getLatitude() { return mLatitude; }

    public double getLongitude() { return mLongitude; }

    public boolean isSwitchChecked() {
        return mIsSwitchChecked;
    }

    public void setSwitchChecked(boolean isChecked) { mIsSwitchChecked = isChecked; }

    public void setLatitude(double latitude) { mLatitude = latitude; }

    public void setLongitude(double longitude) { mLongitude = longitude; }
}
