package com.example.flex.gpsalarm;

/**
 * Created by Flex on 4/15/2017.
 */

public class DestinationOptions {
    private static final String LOG_TAG = DestinationOptions.class.getSimpleName();

    public static final int DEFAULT_PROXIMITY = 100;

    private String mLabel;
    private int mProximity;

    public DestinationOptions() {
        mLabel = "";
        mProximity = DEFAULT_PROXIMITY;
    }

    public DestinationOptions(String label, int proximity) {
        mLabel = label;
        mProximity = proximity;
    }

    public int getProximity() { return mProximity; }

    public String getLabel() { return mLabel; }

    public void setProximity(int proximity) {
        mProximity = proximity;
    }

    public void setLabel(String label) {
        mLabel = label;
    }
}
