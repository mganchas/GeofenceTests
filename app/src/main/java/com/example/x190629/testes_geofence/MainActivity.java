package com.example.x190629.testes_geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    private static final int MIN_TIME_LOCATION_UPDATE = 0; // in milliseconds
    private static final int MIN_DISTANCE_LOCATION_UPDATE = 0; // in meters
    private static final int RADIUS_METERS = 100;
    private static final String PORTUGAL_COUNTRY_CODE = "PT";

    private static List<Location> pointsOfInterestLocations;
    private static Map<String, GeoArea> pointsOfInterest = new HashMap();

    private LocationService locationService;
    private List<Geofence> geofenceList = new ArrayList<>();
    private GeofencingClient geofencingClient;

    private TextView txt_location;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_location = findViewById(R.id.txt_localizacao);

        if (pointsOfInterest == null || pointsOfInterest.isEmpty()) {
            pointsOfInterest = getPointsOfInterest();
        }

        geofencingClient = LocationServices.getGeofencingClient(this);
        createGeoFences();

        // check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "É preciso aceitar...", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,1);
            return;
        }

        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {

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

        if (!LocationService.hasLocationPermissionsAndConnection(this, this)) {
            Toast.makeText(this, "Ativa o GPS por favor", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,1);
            return;
        }

        if (locationService != null) setLocationManager();
    }

    @Override
    protected void onPause() {
        if (locationService != null) locationService.stopLocationUpdates();
        super.onPause();
    }

    private void createGeoFences()
    {
        for (Map.Entry<String, GeoArea> point: pointsOfInterest.entrySet())
        {
            geofenceList.add(
                    new Geofence.Builder()
                            .setRequestId(point.getKey() + "_" + point.getValue().hashCode())
                            .setCircularRegion(point.getValue().getLatitude(), point.getValue().getLongitude(), point.getValue().getRadius())
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                            //.setLoiteringDelay(1)
                            .build()
            );
        }
    }

    private GeofencingRequest getGeofencingRequest()
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setLocationManager()
    {
        locationService.initializeLocationManager
                (
                        new ILocationManagerLocationChanged()
                        {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onLocationChanged(Location location)
                            {
                                String country = null;
                                try {
                                    country = LocationService.getCountryCode(MainActivity.this, location.getLatitude(), location.getLongitude());
                                } catch (IOException ignored) {}

                                GeoArea nearest = LocationService.getNearestPoint(location, pointsOfInterest.values());

                                txt_location.setText("<Atual> \n" +
                                        "\t" + country + " \n" +
                                        "\t" + location.getLatitude() + ", " + location.getLongitude() + " \n" +
                                        "\t" + location.getAccuracy() + " \n" +
                                        "\t" + location.getProvider() + " \n" +
                                        "<Mais perto> \n" +
                                        "\t" + nearest.getLatitude() + ", " + nearest.getLongitude()
                                );

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
                            { }
                        }
                );
    }

    private static Map<String, GeoArea> getPointsOfInterest()
    {
        Map<String, GeoArea> points = new HashMap<>();

        // beja
        points.put("Beja", new GeoArea(38.079048, -7.925615, 2000.00f));

        // humberto delgado
        points.put("Humberto Delgado", new GeoArea(38.765486, -9.142942, 225.36f));
        points.put("Humberto Delgado", new GeoArea(38.769585, -9.139723, 305.45f));
        points.put("Humberto Delgado", new GeoArea(38.767427, -9.133200, 270.06f));
        points.put("Humberto Delgado", new GeoArea(38.769501, -9.128865, 158.31f));
        points.put("Humberto Delgado", new GeoArea(38.775058, -9.133049, 545.70f));
        points.put("Humberto Delgado", new GeoArea(38.782845, -9.134198, 188.22f));
        points.put("Humberto Delgado", new GeoArea(38.786811, -9.133851, 240.36f));
        points.put("Humberto Delgado", new GeoArea(38.790781, -9.131366, 240.36f));
        points.put("Humberto Delgado", new GeoArea(38.794769, -9.129276, 240.36f));

        // bcp edificio 9
        points.put("BCP", new GeoArea(38.743919,-9.306373,10000));

        return points;
    }
}
