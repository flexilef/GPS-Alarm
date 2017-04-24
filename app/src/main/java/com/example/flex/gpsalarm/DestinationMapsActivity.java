package com.example.flex.gpsalarm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.flex.gpsalarm.Services.FetchAddressIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class DestinationMapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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

    private static String TAG = "DestinationMapsActivity";

    private final String EXTRA_KEY_LATITUDE = "LATITUDE";
    private final String EXTRA_KEY_LONGITUDE = "LONGITUDE";
    private final String EXTRA_KEY_ADDRESS = "ADDRESS";
    private final int MAP_PADDING_BOTTOM_DP = 96;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Button mSetDestinationButton;
    private TextView mDestinationText;

    private double mDestinationLatitude;
    private double mDestinationLongitude;
    private double mLastLocationLatitude;
    private double mLastLocationLongitude;
    private String mDestinationAddress;

    //service variables
    private AddressResultReceiver mResultReceiver;
    private Location mLastLocation;
    private Location mDestinationLocation;
    private Handler mUiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_maps);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        mUiHandler = new Handler(Looper.getMainLooper());
        mResultReceiver = new AddressResultReceiver(mUiHandler);

        mDestinationText = (TextView) findViewById(R.id.textView_destination);
        mSetDestinationButton = (Button) findViewById(R.id.button_setDestination);
        mSetDestinationButton.setEnabled(false);

        mDestinationLatitude = getIntent().getDoubleExtra(EXTRA_KEY_LATITUDE, mLastLocationLatitude);
        mDestinationLongitude = getIntent().getDoubleExtra(EXTRA_KEY_LONGITUDE, mLastLocationLongitude);

        mSetDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked on set destination");

                Intent setDestinationIntent = new Intent(v.getContext(), MainActivity.class);
                setDestinationIntent.putExtra(EXTRA_KEY_LATITUDE, mDestinationLatitude);
                setDestinationIntent.putExtra(EXTRA_KEY_LONGITUDE, mDestinationLongitude);
                setDestinationIntent.putExtra(EXTRA_KEY_ADDRESS, mDestinationAddress);

                setResult(RESULT_OK, setDestinationIntent);
                finish();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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

        LatLng initPosition = new LatLng(mDestinationLatitude, mDestinationLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPosition, 15.0f));

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

                startFetchAddressIntentService();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLastLocationLatitude = mLastLocation.getLatitude();
            mLastLocationLongitude = mLastLocation.getLongitude();

            //LatLng initPosition = new LatLng(mDestinationLatitude, mDestinationLongitude);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPosition, 15.0f));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private double getPixelsFromDp(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        return dp * ((double)metrics.densityDpi/ DisplayMetrics.DENSITY_DEFAULT);
    }

    private void enableMyLocation(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
}
