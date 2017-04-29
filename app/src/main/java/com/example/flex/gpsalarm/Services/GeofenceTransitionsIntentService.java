package com.example.flex.gpsalarm.Services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Flex on 4/27/2017.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private final String TAG = "GeofenceTransitions";

    private final String EXTRA_KEY_LATITUDE = "LATITUDE";
    private final String EXTRA_KEY_LONGITUDE = "LONGITUDE";
    private final double DEFAULT_LATITUDE = 0.0;
    private final double DEFAULT_LONGITUDE = 0.0;
    private final float GEOFENCE_RADIUS_IN_METERS = 100;
    private final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 5000;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransition");

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, TAG + ": Geo fence was called");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();

            Log.d(TAG, TAG + ": " + errorCode);
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> trigerringGeofences = geofencingEvent.getTriggeringGeofences();

            for(Geofence geofence : trigerringGeofences) {
                String id = geofence.getRequestId();

                if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    Log.d(TAG, TAG + ": Entering " + id);
                }
                else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    Log.d(TAG, TAG + ": Exiting " + id);
                }
            }
        }
    }
}
