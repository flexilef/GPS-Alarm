package com.example.flex.gpsalarm.Services;

import android.app.IntentService;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import static com.example.flex.gpsalarm.Activities.MainActivity.EXTRA_KEY_DESTINATIONS;

/**
 * Created by Flex on 4/27/2017.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String LOG_TAG = GeofenceTransitionsIntentService.class.getSimpleName();

    private final int NOTIFICATION_ID = 1;
    private final int RELAUNCH_ACTIVITY_CODE = 0;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransition");

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(LOG_TAG, LOG_TAG + ": Geo fence was called");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();

            Log.d(LOG_TAG, LOG_TAG + ": " + errorCode);
            return;
        }

        String destinationListJson = intent.getStringExtra(EXTRA_KEY_DESTINATIONS);
        List<DestinationHeader> destinations = new Gson().fromJson(destinationListJson, new TypeToken<List<DestinationHeader>>() {

        }.getType());

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for(Geofence geofence : triggeringGeofences) {
                String id = geofence.getRequestId();

                if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    Log.d(LOG_TAG, LOG_TAG + ": Entering " + id);

                    String address = getDestinationAddressFromId(id, destinations);
                    sendNotification(address);
                }
                //TODO: remove, only keep for debugging
                else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    Log.d(LOG_TAG, LOG_TAG + ": Exiting " + id);
                }
            }
        }
    }

    /* Helpers */

    //TODO: hardcoded strings should be put in xml
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
