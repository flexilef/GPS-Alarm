package com.example.flex.gpsalarm.Activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.Toast;

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
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        DestinationAdapter.DestinationItemListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    //intent extras keys
    public static final String EXTRA_KEY_LATITUDE = "com.example.flex.gpsalarm.extra.LATITUDE";
    public static final String EXTRA_KEY_LONGITUDE = "com.example.flex.gpsalarm.extra.LONGITUDE";
    public static final String EXTRA_KEY_PROXIMITY = "com.example.flex.gpsalarm.extra.PROXIMITY";
    public static final String EXTRA_KEY_ADDRESS = "com.example.flex.gpsalarm.extra.ADDRESS";
    public static final String EXTRA_KEY_DESTINATIONS = "com.example.flex.gpsalarm.extra.DESTINATIONS";

    private final String SHARED_PREFS_KEY_DESTINATIONS = "com.example.flex.gpsalarm.sharedprefs.DESTINATIONS";
    private final int REQUEST_LOCATION = 0;
    //intent codes
    private final int PICK_DESTINATION_CODE = 1;
    private final int EDIT_DESTINATION_CODE = 2;
    private final int PENDING_INTENT_SENDER = 0;

    private DestinationAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private Location mLastLocation;

    private List<DestinationHeader> mDestinations;
    private List<DestinationOptions> mDestinationOptions;
    private List<String> mGeofencesToDelete;
    private Map<String, Geofence> mRequestIdToGeofence;

    //the destination alarm selected for edit
    private int mEditDestinationIndex;
    //position of destination alarm whose switch was clicked
    private int mSwitchClickedIndex;
    private boolean mIsAddingGeofence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buildGoogleApiClient();

        mGeofencePendingIntent = null;
        mEditDestinationIndex = -1;
        mSwitchClickedIndex = -1;
        mIsAddingGeofence = false;

        mGeofencesToDelete = new ArrayList<>();
        mRequestIdToGeofence = new HashMap<>();
        mDestinations = new ArrayList<>();
        mDestinationOptions = new ArrayList<>();
        mDestinationOptions.add(new DestinationOptions());

        //has to be called after mDestinations is instantiated in order to populate it
        restoreDestinations();

        mAdapter = new DestinationAdapter(this, mDestinations);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView_DestinationsList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Set up listeners
        mAdapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
            @Override
            public void onParentExpanded(int parentPosition) {
                mAdapter.collapseAllParents();
                mAdapter.expandParent(parentPosition);

                //scroll expanded destination lalarm to the top
                mLayoutManager.scrollToPositionWithOffset(parentPosition, 0);
            }

            @Override
            public void onParentCollapsed(int parentPosition) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.FloatingActionButton_AddDestination);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapsIntent = new Intent(view.getContext(), DestinationMapsActivity.class);

                if (mLastLocation != null) {
                    mapsIntent.putExtra(EXTRA_KEY_LATITUDE, mLastLocation.getLatitude());
                    mapsIntent.putExtra(EXTRA_KEY_LONGITUDE, mLastLocation.getLongitude());
                    mapsIntent.putExtra(EXTRA_KEY_PROXIMITY, DestinationOptions.DEFAULT_PROXIMITY);
                }

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
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    //TODO: Add an about page to credit icon artists
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }*/

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
            String address;
            double latitude, longitude;

            address = data.getStringExtra(EXTRA_KEY_ADDRESS);
            latitude = data.getDoubleExtra(EXTRA_KEY_LATITUDE, DestinationMapsActivity.DEFAULT_LATITUDE);
            longitude = data.getDoubleExtra(EXTRA_KEY_LONGITUDE, DestinationMapsActivity.DEFAULT_LONGITUDE);

            if (requestCode == PICK_DESTINATION_CODE) {
                DestinationHeader destination = new DestinationHeader(address, false, mDestinationOptions);
                destination.setLatitude(latitude);
                destination.setLongitude(longitude);

                mDestinations.add(destination);
                int destinationsCount = mDestinations.size();

                mAdapter.notifyParentInserted(destinationsCount - 1);
                mRecyclerView.scrollToPosition(destinationsCount - 1);

            } else if (requestCode == EDIT_DESTINATION_CODE && mEditDestinationIndex >= 0) {
                String oldRequestId = mDestinations.get(mEditDestinationIndex).getId();

                DestinationHeader destination = new DestinationHeader(address, false, mDestinationOptions);
                destination.setLatitude(latitude);
                destination.setLongitude(longitude);

                mDestinations.set(mEditDestinationIndex, destination);

                mAdapter.notifyParentChanged(mEditDestinationIndex);
                mRecyclerView.scrollToPosition(mEditDestinationIndex);

                //remove the old geofence (new geofence will be added when user switches alarm on)
                if (mGoogleApiClient.isConnected()) {
                    removeGeofence(oldRequestId);
                } else {
                    mGeofencesToDelete.add(oldRequestId);
                }
            }
        }
    }

    /* Start DestinationItemListener functions */

    @Override
    //TODO: refactor getProximity() by creating a function within destination header itself
    // and refactor these listener functions by removing childPosition() (only 1 option in list)
    public void onDestinationClicked(int parentPosition) {
        DestinationHeader destination = mDestinations.get(parentPosition);

        double latitude = destination.getLatitude();
        double longitude = destination.getLongitude();
        int proximity = destination.getChildList().get(0).getProximity(); //0 because only 1 option in list

        mEditDestinationIndex = parentPosition;

        Intent mapsIntent = new Intent(this, DestinationMapsActivity.class);
        mapsIntent.putExtra(EXTRA_KEY_LATITUDE, latitude);
        mapsIntent.putExtra(EXTRA_KEY_LONGITUDE, longitude);
        mapsIntent.putExtra(EXTRA_KEY_PROXIMITY, proximity);

        startActivityForResult(mapsIntent, EDIT_DESTINATION_CODE);
    }

    @Override
    public void onDeleteClicked(final int parentPosition, final int childPosition) {
        final DestinationHeader destination = mDestinations.get(parentPosition);
        final String requestId = destination.getId();

        removeGeofence(requestId);
        mDestinations.remove(parentPosition);
        mAdapter.notifyParentRemoved(parentPosition);

        //generate a snackbar to undo deletion
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDestinations.add(parentPosition, destination);
                mAdapter.notifyParentInserted(parentPosition);
                mRecyclerView.scrollToPosition(parentPosition);

                double latitude = destination.getLatitude();
                double longitude = destination.getLongitude();
                float proximity = destination.getChildList().get(childPosition).getProximity();

                if (destination.isSwitchChecked()) {
                    addGeofence(requestId, latitude, longitude, proximity);
                }
            }
        };
        displayUndoDeleteSnackbar(listener);
    }

    @Override
    public void onSwitchClicked(int parentPosition, int childPosition, boolean isChecked) {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        mSwitchClickedIndex = parentPosition;

        DestinationHeader destination = mDestinations.get(parentPosition);
        destination.setSwitchChecked(isChecked);

        String requestId = destination.getId();
        double latitude = destination.getLatitude();
        double longitude = destination.getLongitude();
        float proximity = destination.getChildList().get(childPosition).getProximity();

        if (!isChecked) {
            removeGeofence(requestId);
        }
        if (isChecked) {
            addGeofence(requestId, latitude, longitude, proximity);
        }
    }

    @Override
    public void onProximityChanged(int parentPosition, int childPosition, int proximity) {
        DestinationHeader destination = mDestinations.get(parentPosition);
        destination.getChildList().get(childPosition).setProximity(proximity);

        if(destination.isSwitchChecked()) {
            removeGeofence(destination.getId());
            addGeofence(destination.getId(), destination.getLatitude(), destination.getLongitude(), proximity);
        }
    }

    //TODO: implement this
    @Override
    public void onLabelChanged(int position, String label) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //get last location to pass to map activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();

            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        //remove stale geofences
        if(!mGeofencesToDelete.isEmpty()) {
            int geofenceCount = mGeofencesToDelete.size();

            for(int i = 0; i < geofenceCount; i++) {
                removeGeofence(mGeofencesToDelete.get(i));
                mGeofencesToDelete.remove(i);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permissions enabled!", Toast.LENGTH_SHORT).show();
            }
            else {
                //auto turn off alarm
                //TODO: check if mSwitchClickedIndex is never reset could cause possible bug
                if(mSwitchClickedIndex >= 0) {
                    mDestinations.get(mSwitchClickedIndex).setSwitchChecked(false);
                    mAdapter.notifyParentChanged(mSwitchClickedIndex);
                }

                Toast.makeText(this, "Alarms are not tracked! Enable location permissions!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //TODO: Handle these
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {
        int statusCode = status.getStatusCode();

        if(mIsAddingGeofence) {
            switch (statusCode) {
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE: {
                    Toast.makeText(this, "Alarms are not tracked! Location not available!", Toast.LENGTH_SHORT).show();
                    break;
                }
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES: {
                    Toast.makeText(this, "Could not add alarm. Too many alarms are on! ", Toast.LENGTH_SHORT).show();
                    break;
                }
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS: {
                    Toast.makeText(this, "Could not add alarm. Internal error!", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    }

    /* Helpers */

    private void addGeofence(String requestId, double latitude, double longitude, float radius) {
        mIsAddingGeofence = true;

        mRequestIdToGeofence.put(requestId, new Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();

            return;
        }
        PendingIntent mGeofencePendingIntent = getGeofencePendingIntent();
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                mGeofencePendingIntent
        ).setResultCallback(this);
    }

    private void removeGeofence(String requestId) {
        mIsAddingGeofence = false;

        List<String> geofenceIds = new ArrayList<>();
        geofenceIds.add(requestId);

        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, geofenceIds)
                .setResultCallback(this);

        mRequestIdToGeofence.remove(requestId);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        List<Geofence> geofences = new ArrayList<>(mRequestIdToGeofence.values());
        builder.addGeofences(geofences);

        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if(mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        intent.putExtra(EXTRA_KEY_DESTINATIONS, new Gson().toJson(mDestinations));

        return PendingIntent.getService(this, PENDING_INTENT_SENDER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

    //TODO: put these hardcoded strings into strings.xml
    private void displayUndoDeleteSnackbar(View.OnClickListener listener) {
        CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_main);

        Snackbar snackbar = Snackbar.make(layout, "Destination deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", listener);

        snackbar.show();
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    //save the current list of destinations into shared preferences
    private void storeDestinations() {
        String destinationListJson = new Gson().toJson(mDestinations);

        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_KEY_DESTINATIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putString(SHARED_PREFS_KEY_DESTINATIONS, destinationListJson);

        editor.apply();
    }

    //update mDestinations with any saved destinations
    private void restoreDestinations() {
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_KEY_DESTINATIONS, Context.MODE_PRIVATE);
        String destinationListJson = sharedPrefs.getString(SHARED_PREFS_KEY_DESTINATIONS, "");

        List<DestinationHeader> destinations = new Gson().fromJson(destinationListJson, new TypeToken<List<DestinationHeader>>() {

        }.getType());

        if (destinations != null) {
            mDestinations = destinations;
        }
    }

    //TODO: reimplement these
    /*private void storeGeofences() {
        String geofenceListJson = new Gson().toJson(mRequestIdToGeofence);

        SharedPreferences geofencesSharedPrefs = getSharedPreferences(SHARED_PREFS_GEOFENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor geofencesEditor = geofencesSharedPrefs.edit();

        geofencesEditor.putString(SHARED_PREFS_GEOFENCES_KEY, geofenceListJson);

        geofencesEditor.apply();

        Log.d(LOG_TAG, "store geofences:" + geofenceListJson);
    }*/

    /*private void restoreGeofences() {
        SharedPreferences geofencesSharedPrefs = getSharedPreferences(SHARED_PREFS_GEOFENCES_KEY, Context.MODE_PRIVATE);

        String geofenceListJson = geofencesSharedPrefs.getString(SHARED_PREFS_GEOFENCES_KEY, "");

        Map<String, Geofence> geofences = new Gson().fromJson(geofenceListJson, new TypeToken<Map<String, Geofence>>() {
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
    }*/
}
