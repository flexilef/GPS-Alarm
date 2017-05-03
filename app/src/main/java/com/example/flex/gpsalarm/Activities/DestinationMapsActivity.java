package com.example.flex.gpsalarm.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flex.gpsalarm.DestinationOptions;
import com.example.flex.gpsalarm.R;
import com.example.flex.gpsalarm.Services.FetchAddressIntentService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import static com.example.flex.gpsalarm.Activities.MainActivity.EXTRA_KEY_ADDRESS;
import static com.example.flex.gpsalarm.Activities.MainActivity.EXTRA_KEY_LATITUDE;
import static com.example.flex.gpsalarm.Activities.MainActivity.EXTRA_KEY_LONGITUDE;
import static com.example.flex.gpsalarm.Activities.MainActivity.EXTRA_KEY_PROXIMITY;
import static com.example.flex.gpsalarm.DestinationOptions.DEFAULT_PROXIMITY;

public class DestinationMapsActivity extends FragmentActivity implements
        OnMapReadyCallback {

    public class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mDestinationAddress = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            mDestinationText.setText(mDestinationAddress);

            if (resultCode == FetchAddressIntentService.Constants.FAILURE_RESULT) {
                mSetDestinationButton.setEnabled(false);
            } else {
                mSetDestinationButton.setEnabled(true);
            }
        }
    }

    private static String LOG_TAG = DestinationMapsActivity.class.getSimpleName();
    //Default San Francisco
    public static final double DEFAULT_LATITUDE = 37.7749;
    public static final double DEFAULT_LONGITUDE = -122.4194;

    //to make Google logo visible
    private final int MAP_PADDING_BOTTOM_DP = 120;
    private final int REQUEST_LOCATION = 0;

    private GoogleMap mMap;
    private Button mSetDestinationButton;
    private TextView mDestinationText;
    private CircleOptions mCircleOptions;

    private double mDestinationLatitude;
    private double mDestinationLongitude;
    private int mMarkerRadius;
    private String mDestinationAddress;

    private Location mDestinationLocation;
    private Handler mUiHandler;
    private AddressResultReceiver mResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mUiHandler = new Handler(Looper.getMainLooper());
        mResultReceiver = new AddressResultReceiver(mUiHandler);

        mDestinationText = (TextView) findViewById(R.id.textView_destination);
        mSetDestinationButton = (Button) findViewById(R.id.button_setDestination);
        mSetDestinationButton.setEnabled(false);

        mSetDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setDestinationIntent = new Intent(v.getContext(), MainActivity.class);

                setDestinationIntent.putExtra(EXTRA_KEY_LATITUDE, mDestinationLatitude);
                setDestinationIntent.putExtra(EXTRA_KEY_LONGITUDE, mDestinationLongitude);
                setDestinationIntent.putExtra(EXTRA_KEY_ADDRESS, mDestinationAddress);

                setResult(RESULT_OK, setDestinationIntent);
                finish();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 0, 0, (int) getPixelsFromDp(MAP_PADDING_BOTTOM_DP));
        enableMyLocation(mMap);

        mDestinationLatitude = getIntent().getDoubleExtra(EXTRA_KEY_LATITUDE, DEFAULT_LATITUDE);
        mDestinationLongitude = getIntent().getDoubleExtra(EXTRA_KEY_LONGITUDE, DEFAULT_LONGITUDE);
        mMarkerRadius = getIntent().getIntExtra(EXTRA_KEY_PROXIMITY, DEFAULT_PROXIMITY);

        LatLng initPosition = new LatLng(mDestinationLatitude, mDestinationLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPosition, 15.0f)); //15.0f = city zoom level

        mCircleOptions = new CircleOptions()
                .center(initPosition)
                .radius(mMarkerRadius)
                .fillColor(R.color.colorAccentLight);
        mMap.addCircle(mCircleOptions);

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                LatLng cameraCenter = mMap.getCameraPosition().target;
                mDestinationLatitude = cameraCenter.latitude;
                mDestinationLongitude = cameraCenter.longitude;

                mDestinationText.setText(getString(R.string.updating_location));
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mDestinationLocation = new Location("");
                mDestinationLocation.setLatitude(mDestinationLatitude);
                mDestinationLocation.setLongitude(mDestinationLongitude);

                LatLng newPosition = new LatLng(mDestinationLatitude, mDestinationLongitude);
                mCircleOptions.center(newPosition);

                mMap.clear();
                mMap.addCircle(mCircleOptions);

                startFetchAddressIntentService();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permissions enabled!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Cannot track your location. Enable location permissions!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /* Helpers */

    private double getPixelsFromDp(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        return dp * ((double)metrics.densityDpi/ DisplayMetrics.DENSITY_DEFAULT);
    }

    private void enableMyLocation(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();

            return;
        }
        map.setMyLocationEnabled(true);
    }

    private void startFetchAddressIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mDestinationLocation);

        startService(intent);
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }
}
