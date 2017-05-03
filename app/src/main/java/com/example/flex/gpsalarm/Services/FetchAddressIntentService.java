package com.example.flex.gpsalarm.Services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.flex.gpsalarm.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Flex on 4/22/2017.
 */

public class FetchAddressIntentService extends IntentService {

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME = "package com.example.flex.gpsalarm";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    }

    private final String LOG_TAG = FetchAddressIntentService.class.getSimpleName();

    private ResultReceiver mReceiver;
    private Location mLocation;

    private String mErrorMessage;

    public FetchAddressIntentService() {
        super("FetchAddress");
        mErrorMessage = "";
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        mLocation = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
        }
        catch(IOException ioException) {
            mErrorMessage = getString(R.string.service_not_available);
            //Log.e(LOG_TAG, mErrorMessage, ioException);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            mErrorMessage = getString(R.string.invalid_lat_long_used);
            /*Log.e(LOG_TAG, mErrorMessage + ". " +
                    "Latitude = " + mLocation.getLatitude() +
                    ", Longitude = " + mLocation.getLongitude(), illegalArgumentException);*/
        }

        // Handle case where no address was found
        if (addresses == null || addresses.size() == 0) {
            if (mErrorMessage.isEmpty()) {
                mErrorMessage = getString(R.string.no_address_found);
                //Log.e(LOG_TAG, mErrorMessage);
            }

            deliverResultToReceiver(Constants.FAILURE_RESULT, mErrorMessage);
        } else {
            Address address = addresses.get(0);
            List<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            //Log.i(LOG_TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"), addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);

        mReceiver.send(resultCode, bundle);
    }
}
