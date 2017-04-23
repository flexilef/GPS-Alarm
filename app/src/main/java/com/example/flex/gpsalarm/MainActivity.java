package com.example.flex.gpsalarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
    DestinationAdapter.DestinationItemListener {

    private static String TAG = "MainActivity";

    private final String EXTRA_KEY_LATITUDE = "LATITUDE";
    private final String EXTRA_KEY_LONGITUDE = "LONGITUDE";
    private final String SHARED_PREFS_DESTINATIONS_KEY = "com.example.flex.gpsalarm.DESTINATIONS";
    private final double DEFAULT_LATITUDE = 0.0;
    private final double DEFAULT_LONGITUDE = 0.0;
    private final int PICK_DESTINATION_CODE = 1;
    private final int EDIT_DESTINATION_CODE = 2;

    private List<DestinationHeader> mDestinations;
    private List<DestinationOptions> mOptions;

    private DestinationAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private int mEditDestinationIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ON CREATE");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mOptions = new ArrayList<>();
        mDestinations = new ArrayList<>();
        mOptions.add(new DestinationOptions("Label 1"));

        restoreDestinations();
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView_DestinationsList);
        mAdapter = new DestinationAdapter(this, mDestinations);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //button code
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
    protected void onResume() {
        Log.d(TAG, "on resume");

        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "on stop");

        super.onStop();
        storeDestinations();
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
        if(resultCode == RESULT_OK) {
            double latitude, longitude;
            latitude = data.getDoubleExtra(EXTRA_KEY_LATITUDE, DEFAULT_LATITUDE);
            longitude = data.getDoubleExtra(EXTRA_KEY_LONGITUDE, DEFAULT_LONGITUDE);

            if(requestCode == PICK_DESTINATION_CODE) {
                DestinationHeader destination = new DestinationHeader(latitude + "," + longitude, false, mOptions);
                destination.setLatitude(latitude);
                destination.setLongitude(longitude);

                mDestinations.add(destination);

                mAdapter.notifyParentInserted(mDestinations.size()-1);
                mRecyclerView.scrollToPosition(mDestinations.size()-1);
            }
            else if(requestCode == EDIT_DESTINATION_CODE && mEditDestinationIndex >= 0) {
                DestinationHeader destination = new DestinationHeader(latitude + "," + longitude, false, mOptions);
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

        mDestinations.remove(position);
        mAdapter.notifyParentRemoved(position);

        //generate an undo snackbar
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDestinations.add(position, destination);
                mAdapter.notifyParentInserted(position);
            }
        };
        displayUndoDeleteSnackbar(listener);
    }

    @Override
    public void onSwitchClicked(int position, boolean isChecked) {
        //TODO: start a service to check for alarm going off
        mDestinations.get(position).setSwitchChecked(isChecked);
        // Uncommenting the code below will cause a crash because of
        // circular code involving the switch listener in view holder and
        // the onBind function in adapter
        // The code below is unecessary anyways since switch listener
        // updates the UI so there isn't a need to notify the adapter to update UI
        // mAdapter.notifyParentChanged(position);
    }

    /* Helpers */

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

    //update mDestinations with any saved destinations
    private void restoreDestinations() {
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_DESTINATIONS_KEY, Context.MODE_PRIVATE);
        String destinationListJson = sharedPrefs.getString(SHARED_PREFS_DESTINATIONS_KEY, "");

        List<DestinationHeader> destinations = new Gson().fromJson(destinationListJson, new TypeToken<List<DestinationHeader>>() {

        }.getType());

        if(destinations != null) {
            mDestinations = destinations;
        }
    }
}
