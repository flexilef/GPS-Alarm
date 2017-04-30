package com.example.flex.gpsalarm.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.flex.gpsalarm.Activities.MainActivity;
import com.example.flex.gpsalarm.DestinationHeader;
import com.example.flex.gpsalarm.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
    private final int NOTIFICATION_ID = 1;
    private final int RELAUNCH_ACTIVITY_CODE = 0;
    private final String EXTRA_KEY_DESTINATIONS = "DESTINATIONS";

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
            return;
        }

        String destinationListJson = intent.getStringExtra(EXTRA_KEY_DESTINATIONS);
        List<DestinationHeader> destinations = new Gson().fromJson(destinationListJson, new TypeToken<List<DestinationHeader>>() {

        }.getType());

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> trigerringGeofences = geofencingEvent.getTriggeringGeofences();

            for(Geofence geofence : trigerringGeofences) {
                String id = geofence.getRequestId();

                if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    Log.d(TAG, TAG + ": Entering " + id);

                    String address = getDestinationAddressFromId(id, destinations);
                    sendNotification(address);
                }
                else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    Log.d(TAG, TAG + ": Exiting " + id);
                }
            }
        }
    }

    private void sendNotification(String address) {
        Intent mainIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(mainIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(RELAUNCH_ACTIVITY_CODE, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.alarm_clock_50)
                .setColor(Color.BLUE)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setContentTitle("Destination Reached!")
                .setContentText(address)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private String getDestinationAddressFromId(String requestId, List<DestinationHeader> destinations) {
        for(DestinationHeader destination : destinations) {
            if(destination.getId().equals(requestId)) {
                return destination.getDestinationAddress();
            }
        }

        return "";
    }
}
