package com.example.flex.gpsalarm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DestinationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static String TAG = "DestinationMapsActivity";
    private final String EXTRA_KEY_LATITUDE = "LATITUDE";
    private final String EXTRA_KEY_LONGITUDE = "LONGITUDE";
    private final int MAP_PADDING_BOTTOM_DP = 96;

    private GoogleMap mMap;
    //private LocationManager mLocationManager;

    private Button mSetDestinationButton;
    private TextView mDestinationText;
    private double mDestinationLatitude;
    private double mDestinationLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_maps);

        //mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mDestinationText = (TextView) findViewById(R.id.textView_destination);
        mSetDestinationButton = (Button) findViewById(R.id.button_setDestination);
        mDestinationLatitude = getIntent().getDoubleExtra(EXTRA_KEY_LATITUDE, 0.0);
        mDestinationLongitude = getIntent().getDoubleExtra(EXTRA_KEY_LONGITUDE, 0.0);;

        mSetDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked on set destination");

                Intent setDestinationIntent = new Intent(v.getContext(), MainActivity.class);
                setDestinationIntent.putExtra(EXTRA_KEY_LATITUDE, mDestinationLatitude);
                setDestinationIntent.putExtra(EXTRA_KEY_LONGITUDE, mDestinationLongitude);

                setResult(RESULT_OK, setDestinationIntent);
                finish();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        mMap.setPadding(0, 0, 0, (int)getPixelsFromDp(MAP_PADDING_BOTTOM_DP));

        LatLng initPosition = new LatLng(mDestinationLatitude, mDestinationLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initPosition));

        LatLng cameraCenter = mMap.getCameraPosition().target;
        mDestinationLatitude = cameraCenter.latitude;
        mDestinationLongitude = cameraCenter.longitude;
        mDestinationText.setText(mDestinationLatitude + ", " + mDestinationLongitude);

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                LatLng cameraCenter = mMap.getCameraPosition().target;
                mDestinationLatitude = cameraCenter.latitude;
                mDestinationLongitude = cameraCenter.longitude;

                mDestinationText.setText(mDestinationLatitude + ", " + mDestinationLongitude);
            }
        });

        /*
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                centerMarker.setPosition(new LatLng(longitude, latitude));
                mDestinationText.setText(longitude + ", " + latitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        showLastKnownLocation();
        */
    }

    private void showLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        /*Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            LatLng myLocation = new LatLng(location.getLongitude(), location.getLatitude());
            mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

            mDestinationText.setText(location.getLongitude() + ", " + location.getLatitude());
        } else {
            mDestinationText.setText("Unknown");
        }*/
    }

    private double getPixelsFromDp(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        return dp * ((double)metrics.densityDpi/ DisplayMetrics.DENSITY_DEFAULT);
    }
}
