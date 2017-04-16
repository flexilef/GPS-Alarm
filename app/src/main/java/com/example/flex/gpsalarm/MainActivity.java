package com.example.flex.gpsalarm;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
    DestinationAdapter.DestinationItemListener {
    public static String TAG = "MainActivity";

    private List<DestinationOptions> mOptions;
    private List<DestinationHeader> mDestinations;

    private DestinationAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ImageButton mDeleteButton;

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

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.RecyclerView_DestinationsList);
        mAdapter = new DestinationAdapter(this, mDestinations);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //button code
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.FloatingActionButton_AddDestination);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: replace with map activity
                Log.d(TAG, "fab clicked");
                mDestinations.add(new DestinationHeader("New Destination Address", true, mOptions));
                mAdapter.notifyParentInserted(mDestinations.size()-1);
                recyclerView.scrollToPosition(mDestinations.size()-1);
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
    }

    @Override
    public void onDeleteClicked(int position) {
        Log.d(TAG, "Delete Position " + position);
        mDestinations.remove(position);
        mAdapter.notifyParentRemoved(position);
    }
}
