package com.example.flex.gpsalarm;

/**
 * Created by Flex on 4/9/2017.
 */

public class DestinationRowItem {

    private String mDestinationAddress;
    private boolean mIsDestinationSet;

    public DestinationRowItem(String address, boolean isDestinationSet) {
        mDestinationAddress = address;
        mIsDestinationSet = isDestinationSet;
    }

    public String getDestination() {
        return mDestinationAddress;
    }

    public boolean isDestinationSet() {
        return mIsDestinationSet;
    }
}
