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

    private List<DestinationOptions> options;
    private List<DestinationHeader> destinations;

    private ImageButton mDeleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        options = new ArrayList<>();
        destinations = new ArrayList<>();

        options.add(new DestinationOptions("Label 1"));
        destinations.add(new DestinationHeader("250 Flood Ave", false, options));
        destinations.add(new DestinationHeader("1600 Holloway Ave", false, options));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.RecyclerView_DestinationsList);
        //final DestinationAdapter adapter = new DestinationAdapter(this, destinations, this);
        final DestinationAdapter adapter = new DestinationAdapter(this, destinations);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //button code
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.FloatingActionButton_AddDestination);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        //TODO
        Log.d(TAG, "Destination Position " + position);
    }

    @Override
    public void onDeleteClicked(int position) {
        //TODO
        Log.d(TAG, "Delete Position " + position);
    }
}
