package com.example.flex.gpsalarm;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.List;
import java.util.UUID;

/**
 * Created by Flex on 4/15/2017.
 */

public class DestinationHeader implements Parent<DestinationOptions> {

    private final double DEFAULT_LATITUDE = 0.0;
    private final double DEFAULT_LONGITUDE = 0.0;

    private List<DestinationOptions> mDestinationOptions;

    private String id;
    private String mDestinationAddress;
    private double mLatitude;
    private double mLongitude;
    private boolean mIsSwitchChecked;

    public DestinationHeader(String destinationAddress, boolean isChecked, List<DestinationOptions> options) {
        id = UUID.randomUUID().toString();
        mDestinationOptions = options;
        mDestinationAddress = destinationAddress;
        mIsSwitchChecked = isChecked;
        mLatitude = DEFAULT_LATITUDE;
        mLongitude = DEFAULT_LONGITUDE;
    }

    @Override
    public List<DestinationOptions> getChildList() {
        return mDestinationOptions;
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

    public String getId() { return id; }

    public boolean isSwitchChecked() {
        return mIsSwitchChecked;
    }

    public void setSwitchChecked(boolean isChecked) { mIsSwitchChecked = isChecked; }

    public void setLatitude(double latitude) { mLatitude = latitude; }

    public void setLongitude(double longitude) { mLongitude = longitude; }
}
