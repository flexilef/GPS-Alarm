package com.example.flex.gpsalarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
    DestinationAdapter.DestinationItemListener {
    private static String TAG = "MainActivity";

    private List<DestinationHeader> mDestinations;
    private List<DestinationOptions> mOptions;

    private DestinationAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mOptions = new ArrayList<>();
        mDestinations = new ArrayList<>();

        mOptions.add(new DestinationOptions("Label 1"));
        mDestinations.add(new DestinationHeader("250 Flood Ave", false, mOptions));
        mDestinations.add(new DestinationHeader("1600 Holloway Ave", false, mOptions));

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView_DestinationsList);
        mAdapter = new DestinationAdapter(this, mDestinations);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //button code
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.FloatingActionButton_AddDestination);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: replace with map activity
                Log.d(TAG, "fab clicked");
                
                mDestinations.add(new DestinationHeader("New Destination Address", false, mOptions));
                mAdapter.notifyParentInserted(mDestinations.size()-1);
                mRecyclerView.scrollToPosition(mDestinations.size()-1);
            }
        });
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
    public void onDestinationClicked(int position) {
        //TODO: start map activity
        Log.d(TAG, "Destination Position " + position);
        Intent mapsIntent = new Intent(this, DestinationMapsActivity.class);
        startActivity(mapsIntent);
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
        mDestinations.get(position).setSwitchChecked(isChecked);
        // Uncommenting the code below will cause a crash because of
        // circular code involving the switch listener in view holder and
        // the onBind function in adapter
        // The code below is unecessary anyways since switch listener
        // updates the UI so there isn't a need to notify the adapter to update UI
        // mAdapter.notifyParentChanged(position);
    }

    private void displayUndoDeleteSnackbar(View.OnClickListener listener) {
        CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_main);

        Snackbar snackbar = Snackbar.make(layout, "Destination deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", listener);

        snackbar.show();
    }
}
