package com.example.x190629.testes_geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.x190629.testes_geofence.entities.NearestPoint;
import com.example.x190629.testes_geofence.entities.PointsOfInterest;
import com.example.x190629.testes_geofence.factories.BCPWorkerFactory;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerLocationChanged;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerProviderDisabled;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerProviderEnabled;
import com.example.x190629.testes_geofence.services.location.LocationHandlerService;
import com.example.x190629.testes_geofence.workers.LocationWorker;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int MIN_TIME_LOCATION_UPDATE = 0; // in milliseconds
    private static final int MIN_DISTANCE_LOCATION_UPDATE = 0; // in meters
    private static final String PORTUGAL_COUNTRY_CODE = "PT";

    private static final String TAG = MainActivity.class.getName();

    //private Intent locationServiceIntent;
    //private BackgroundService backgroundService;
    private LocationHandlerService locationHandlerService;

    private TextView txt_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_location = findViewById(R.id.txt_localizacao);

        findViewById(R.id.btn_add_bcp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"add bcp click");

                PointsOfInterest.addBCP();
            }
        });

        findViewById(R.id.btn_remove_bcp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PointsOfInterest.removeBCP();
            }
        });

        findViewById(R.id.btn_getlocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                locationHandlerService = new LocationHandlerService(
                        MainActivity.this,
                        MIN_TIME_LOCATION_UPDATE,
                        MIN_DISTANCE_LOCATION_UPDATE
                );

                // check location permission
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(MainActivity.this, "É preciso aceitar...", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }

                setLocationManager();

                if (!LocationHandlerService.hasLocationPermissionsAndConnection(MainActivity.this)) {
                    LocationHandlerService.connectToGooglePlayServices(MainActivity.this);
                }
            }
        });

                new BCPWorkerFactory().create(LocationWorker.class, LocationWorker.getMinutes(), LocationWorker.getWorkerConstraints());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // check boot permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, 2);
            return;
        }

        if (locationHandlerService != null) setLocationManager();
    }

    @Override
    protected void onPause() {
        if (locationHandlerService != null) locationHandlerService.stopLocationUpdates();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //stopService(locationServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    private void setLocationManager() {
        locationHandlerService.initializeLocationManager
        (
                new ILocationManagerLocationChanged() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onLocationChanged(Location location) {
                        String country = null, locality = null;
                        try {
                            Address address = LocationHandlerService.getLocationAddress(MainActivity.this, location.getLatitude(), location.getLongitude());
                            if (address != null) {
                                country = address.getCountryCode();
                                locality = address.getLocality();
                            }
                        } catch (IOException ignored) {
                        }

                        NearestPoint nearest = LocationHandlerService.getNearestPoint(location, PointsOfInterest.airports);
                        boolean isInside = nearest.getDistance() <= nearest.getGeoArea().getRadius();

                        txt_location.setText("<Localização Atual> \n" +
                                "\t" + country + " \n" +
                                "\t" + locality + " \n" +
                                "\t" + location.getLatitude() + ", " + location.getLongitude() + " \n" +
                                "\t" + location.getAccuracy() + " \n" +
                                "\t" + location.getProvider() + " \n" +
                                "<Geofence mais perto> \n" +
                                "\t" + nearest.getAirportName() + " \n" +
                                "\t" + nearest.getDistance() + " \n" +
                                "\t" + nearest.getGeoArea().getLatitude() + ", " + nearest.getGeoArea().getLongitude() + " \n" +
                                "<Dentro da geofence?> \n" +
                                "\t" + isInside
                        );

                    }
                },
                new ILocationManagerProviderEnabled() {
                    @Override
                    public void onProviderEnabled(String provider) {
                        setLocationManager();
                    }
                },
                new ILocationManagerProviderDisabled() {
                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }
        );
    }
}
