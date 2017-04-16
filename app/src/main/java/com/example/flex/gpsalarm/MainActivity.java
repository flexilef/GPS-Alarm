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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //private List<DestinationRowItem> itemList;
    //private RecyclerView.Adapter adapter;

    private List<DestinationOptions> options;
    private List<DestinationHeader> destinations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*
        itemList = new ArrayList<>();
        itemList.add(new DestinationRowItem("1600 Hollowaysadfsdafsdafsdafsdfsdafsdafsda Ave", true));
        itemList.add(new DestinationRowItem("250 Ave", false));
        itemList.add(new DestinationRowItem("330 Grafton Ave", false));
        itemList.add(new DestinationRowItem("123 Portrero Ave", false));
        itemList.add(new DestinationRowItem("456 Russia Ave", false));
        itemList.add(new DestinationRowItem("789 Paris Ave", false));
        itemList.add(new DestinationRowItem("222 Hamilton Ave", false));
        itemList.add(new DestinationRowItem("1111 Geary St", false));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.RecyclerView_DestinationsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DestinationListAdapter(itemList);
        recyclerView.addItemDecoration(new DestinationDividerItemDecoration(this));
        recyclerView.setAdapter(adapter);
        */

        options = new ArrayList<>();
        destinations = new ArrayList<>();

        options.add(new DestinationOptions("Label 1"));
        destinations.add(new DestinationHeader("250 Flood Ave", false, options));
        destinations.add(new DestinationHeader("1600 Holloway Ave", false, options));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.RecyclerView_DestinationsList);
        DestinationAdapter adapter = new DestinationAdapter(this, destinations);
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
}
