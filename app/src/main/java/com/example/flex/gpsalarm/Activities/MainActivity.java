package com.example.flex.gpsalarm.Activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.example.flex.gpsalarm.DestinationAdapter;
import com.example.flex.gpsalarm.DestinationHeader;
import com.example.flex.gpsalarm.DestinationOptions;
import com.example.flex.gpsalarm.R;
import com.example.flex.gpsalarm.Services.GeofenceTransitionsIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements DestinationAdapter.DestinationItemListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static String TAG = "MainActivity";

    private final String SHARED_PREFS_DESTINATIONS_KEY = "com.example.flex.gpsalarm.DESTINATIONS";
    private final String SHARED_PREFS_GEOFENCES_KEY = "com.example.flex.gpsalarm.GEOFENCES";
    private final double DEFAULT_LATITUDE = 0.0;
    private final double DEFAULT_LONGITUDE = 0.0;

    //intent codes
    private final int PICK_DESTINATION_CODE = 1;
    private final int EDIT_DESTINATION_CODE = 2;
    private final int START_GEOFENCE_CODE = 3;

    //intent extras
    private final String EXTRA_KEY_LATITUDE = "LATITUDE";
    private final String EXTRA_KEY_LONGITUDE = "LONGITUDE";
    private final String EXTRA_KEY_ADDRESS = "ADDRESS";

    private DestinationAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;

    private Map<String, Geofence> mRequestIdToGeofence;
    private Map<Integer, String> mPositionToRequestId;
    private List<DestinationHeader> mDestinations;
    private List<DestinationOptions> mOptions;

    private int mEditDestinationIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGeofencePendingIntent = null;
        buildGoogleApiClient();

        mRequestIdToGeofence = new HashMap<>();
        mOptions = new ArrayList<>();
        mDestinations = new ArrayList<>();
        mOptions.add(new DestinationOptions("Label 1"));

        restoreDestinations();
        mAdapter = new DestinationAdapter(this, mDestinations);
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView_DestinationsList);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //adapter listeners
        mAdapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
            @Override
            public void onParentExpanded(int parentPosition) {
                mAdapter.collapseAllParents();
                mAdapter.expandParent(parentPosition);

                //to make last destination option visible and scrolled when parent item is expanded
                mLayoutManager.scrollToPositionWithOffset(parentPosition, 0);
            }

            @Override
            public void onParentCollapsed(int parentPosition) {

            }
        });

        //button listeners
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.FloatingActionButton_AddDestination);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "fab clicked");
                Intent mapsIntent = new Intent(view.getContext(), DestinationMapsActivity.class);
                startActivityForResult(mapsIntent, PICK_DESTINATION_CODE);
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        storeDestinations();
        storeGeofences();
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //Save the expand/collapse state of the destinations in recycler view
        mAdapter.onSaveInstanceState(savedInstanceState);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //Restore the expand/collapse state of the destinations in recycler view
        mAdapter.onRestoreInstanceState(savedInstanceState);

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            double latitude, longitude;
            String address = data.getStringExtra(EXTRA_KEY_ADDRESS);
            latitude = data.getDoubleExtra(EXTRA_KEY_LATITUDE, DEFAULT_LATITUDE);
            longitude = data.getDoubleExtra(EXTRA_KEY_LONGITUDE, DEFAULT_LONGITUDE);

            if (requestCode == PICK_DESTINATION_CODE) {
                DestinationHeader destination = new DestinationHeader(address, false, mOptions);
                destination.setLatitude(latitude);
                destination.setLongitude(longitude);

                mDestinations.add(destination);

                mAdapter.notifyParentInserted(mDestinations.size() - 1);
                mRecyclerView.scrollToPosition(mDestinations.size() - 1);
            } else if (requestCode == EDIT_DESTINATION_CODE && mEditDestinationIndex >= 0) {
                DestinationHeader destination = new DestinationHeader(address, false, mOptions);
                destination.setLatitude(latitude);
                destination.setLongitude(longitude);

                mDestinations.set(mEditDestinationIndex, destination);

                mAdapter.notifyParentChanged(mEditDestinationIndex);
                mRecyclerView.scrollToPosition(mEditDestinationIndex);
            }
        }
    }

    /* Start DestinationItemListener functions */

    @Override
    //pass extra in the intent back with the position of the destination clicked
    public void onDestinationClicked(int position) {
        Log.d(TAG, "Destination Position " + position);

        mEditDestinationIndex = position;
        double latitude = mDestinations.get(position).getLatitude();
        double longitude = mDestinations.get(position).getLongitude();

        Intent mapsIntent = new Intent(this, DestinationMapsActivity.class);
        mapsIntent.putExtra(EXTRA_KEY_LATITUDE, latitude);
        mapsIntent.putExtra(EXTRA_KEY_LONGITUDE, longitude);
        startActivityForResult(mapsIntent, EDIT_DESTINATION_CODE);
    }

    @Override
    public void onDeleteClicked(final int position) {
        Log.d(TAG, "Delete Position " + position);

        final DestinationHeader destination = mDestinations.get(position);

        List<String> geofenceIds = new ArrayList<>();
        //taking advantage of position also being used as the geofence request ids
        geofenceIds.add(String.valueOf(position));

        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, geofenceIds)
                .setResultCallback(this);
        mRequestIdToGeofence.remove(position);

        mDestinations.remove(position);
        mAdapter.notifyParentRemoved(position);

        //generate an undo snackbar
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDestinations.add(position, destination);
                mAdapter.notifyParentInserted(position);
                mRecyclerView.scrollToPosition(position);
            }
        };
        displayUndoDeleteSnackbar(listener);
    }

    @Override
    public void onSwitchClicked(int position, boolean isChecked) {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        DestinationHeader destination = mDestinations.get(position);
        destination.setSwitchChecked(isChecked);

        mGeofencePendingIntent = getGeofencePendingIntent();

        if (!isChecked) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mGeofencePendingIntent)
                    .setResultCallback(this);

            mRequestIdToGeofence.remove(position);
        }

        //add the geofence
        if (isChecked) {
            double latitude = destination.getLatitude();
            double longitude = destination.getLongitude();
            float radius = 100;

            mRequestIdToGeofence.put(position, new Geofence.Builder()
                    .setRequestId("" + position)
                    .setCircularRegion(latitude, longitude, radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    mGeofencePendingIntent
            ).setResultCallback(this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        restoreGeofences();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    /* Helpers */

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        List<Geofence> geofences = new ArrayList<>(mRequestIdToGeofence.values());
        builder.addGeofences(geofences);

        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            Log.d(TAG, "pending intent is NOT null");
            return mGeofencePendingIntent;
        }
        Log.d(TAG, "pending intent is null");

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private synchronized void buildGoogleApiClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();
        }
    }

    private void displayUndoDeleteSnackbar(View.OnClickListener listener) {
        CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_main);

        Snackbar snackbar = Snackbar.make(layout, "Destination deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", listener);

        snackbar.show();
    }

    //save the current list of destinations into shared preferences
    private void storeDestinations() {
        String destinationListJson = new Gson().toJson(mDestinations);

        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_DESTINATIONS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putString(SHARED_PREFS_DESTINATIONS_KEY, destinationListJson);

        editor.apply();

        Log.d(TAG, "store destination:" + destinationListJson);
    }

    private void storeGeofences() {
        String geofenceListJson = new Gson().toJson(mRequestIdToGeofence);

        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_GEOFENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putString(SHARED_PREFS_DESTINATIONS_KEY, geofenceListJson);

        editor.apply();

        Log.d(TAG, "store geofences:" + geofenceListJson);
    }

    //update mDestinations with any saved destinations
    private void restoreDestinations() {
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_DESTINATIONS_KEY, Context.MODE_PRIVATE);
        String destinationListJson = sharedPrefs.getString(SHARED_PREFS_DESTINATIONS_KEY, "");

        List<DestinationHeader> destinations = new Gson().fromJson(destinationListJson, new TypeToken<List<DestinationHeader>>() {

        }.getType());

        if (destinations != null) {
            mDestinations = destinations;
        }
    }

    private void restoreGeofences() {
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_GEOFENCES_KEY, Context.MODE_PRIVATE);
        String geofenceListJson = sharedPrefs.getString(SHARED_PREFS_GEOFENCES_KEY, "");

        Map<Integer, Geofence> geofences = new Gson().fromJson(geofenceListJson, new TypeToken<Map<Integer, Geofence>>() {

        }.getType());

        if (geofences != null) {
            mRequestIdToGeofence = geofences;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    mGeofencePendingIntent
            ).setResultCallback(this);
        }
    }
}
