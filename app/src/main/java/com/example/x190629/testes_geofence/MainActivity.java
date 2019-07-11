package com.example.x190629.testes_geofence;

import android.Manifest;
import android.app.PendingIntent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private static final int MIN_TIME_LOCATION_UPDATE = 0; // in milliseconds
    private static final int MIN_DISTANCE_LOCATION_UPDATE = 0; // in meters
    private static final int RADIUS_METERS = 100;
    private static final String PORTUGAL_COUNTRY_CODE = "PT";

    private static List<Airport> airports = new ArrayList<>();

    private LocationService locationService;
    private PendingIntent geofencePendingIntent;
    private List<Geofence> geofenceList = new ArrayList<>();
    private GeofencingClient geofencingClient;

    private TextView txt_location;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_location = findViewById(R.id.txt_localizacao);

        if (airports == null || airports.isEmpty()) {
            airports = getAirports();
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
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Sucesso em adicionar geofence", Toast.LENGTH_SHORT).show();
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
        for (Airport airport: airports)
        {
            for (GeoArea geoArea : airport.getFence())
            {
                geofenceList.add(
                        new Geofence.Builder()
                                .setRequestId(airport.getName() + "_" + geoArea.hashCode())
                                .setCircularRegion(geoArea.getLatitude(), geoArea.getLongitude(), geoArea.getRadius())
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                                .setLoiteringDelay(1)
                                .build()
                );
            }
        }
    }

    private void setLocationManager()
    {
        locationService.initializeLocationManager
        (
                new ILocationManagerLocationChanged()
                {
                    @Override
                    public void onLocationChanged(Location location)
                    {
                        String country = getCountryCode(location.getLatitude(), location.getLongitude());
                        txt_location.setText(country + ": \n" +
                                location.getLatitude() + ", \n" +
                                location.getLongitude() + ", \n" +
                                location.getAccuracy() + ", \n" +
                                location.getProvider()
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

    private GeofencingRequest getGeofencingRequest()
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
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
        geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }


    private String getCountryCode(double latitude, double longitude)
    {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getCountryCode();
            }
        } catch (IOException ioe) { }
        return null;
    }

    private static List<Airport> getAirports()
    {
        List<Airport> airports = new ArrayList<>();
        List<GeoArea> fence = new ArrayList<>();

        // beja
        fence.add(new GeoArea(38.079048, -7.925615, 2000.00f));
        airports.add(new Airport("Beja", fence));

        // humberto delgado
        fence.clear();
        fence.add(new GeoArea(38.765486, -9.142942, 225.36f));
        fence.add(new GeoArea(38.769585, -9.139723, 305.45f));
        fence.add(new GeoArea(38.767427, -9.133200, 270.06f));
        fence.add(new GeoArea(38.769501, -9.128865, 158.31f));
        fence.add(new GeoArea(38.775058, -9.133049, 545.70f));
        fence.add(new GeoArea(38.782845, -9.134198, 188.22f));
        fence.add(new GeoArea(38.786811, -9.133851, 240.36f));
        fence.add(new GeoArea(38.790781, -9.131366, 240.36f));
        fence.add(new GeoArea(38.794769, -9.129276, 240.36f));
        airports.add(new Airport("Humberto Delgado", fence));

        // bcp edificio 9
        fence.clear();
        fence.add(new GeoArea(38.743919,-9.306373,100));
        airports.add(new Airport("BCP", fence));

        return airports;
    }
}
