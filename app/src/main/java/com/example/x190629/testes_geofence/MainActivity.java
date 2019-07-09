package com.example.x190629.testes_geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private static final int MIN_TIME_LOCATION_UPDATE = 0; // in milliseconds
    private static final int MIN_DISTANCE_LOCATION_UPDATE = 0; // in meters
    private static int RADIUS_METERS = 100;

    private LocationService locationService;
    private PendingIntent geofencePendingIntent;
    private List<Geofence> geofenceList = new ArrayList<>();
    private GeofencingClient geofencingClient;

    private TextView txt_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("bcp_edificio9")
                .setCircularRegion(38.744112, -9.306677, RADIUS_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(0)
                .build());

        // check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,1);
            return;
        }

        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Deu para adicionar geofence", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Falhou adicionar geofence", Toast.LENGTH_SHORT).show();
                    }
                });

        locationService = new LocationService(
                this,
                this,
                MIN_TIME_LOCATION_UPDATE,
                MIN_DISTANCE_LOCATION_UPDATE
        );
        setLocationManager();

        txt_location = findViewById(R.id.txt_localizacao);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationService != null) setLocationManager();
    }

    @Override
    protected void onPause() {
        if (locationService != null) locationService.stopLocationUpdates();
        super.onPause();
    }

    private void setLocationManager()
    {
        locationService.initializeLocationManager
        (
                new ILocationManagerLocationChanged() {
                    @Override
                    public void onLocationChanged(Location location) {
                        txt_location.setText(location.getLatitude() + ", " + location.getLongitude());
                    }
                },
                new ILocationManagerProviderEnabled()
                {
                    @Override
                    public void onProviderEnabled(String provider)
                    {
                        setLocationManager();
                    }
                },
                new ILocationManagerProviderDisabled()
                {
                    @Override
                    public void onProviderDisabled(String provider)
                    {
                        Toast.makeText(MainActivity.this, "No GPS buddy", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private GeofencingRequest getGeofencingRequest()
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }
}
