package com.example.x190629.testes_geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.x190629.testes_geofence.entities.GeoArea;
import com.example.x190629.testes_geofence.entities.NearestPoint;
import com.example.x190629.testes_geofence.services.LocationHandlerService;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerLocationChanged;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerProviderDisabled;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerProviderEnabled;
import com.example.x190629.testes_geofence.services.backgroundservices.BackgroundService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    private static final int MIN_TIME_LOCATION_UPDATE = 0; // in milliseconds
    private static final int MIN_DISTANCE_LOCATION_UPDATE = 0; // in meters
    private static final String PORTUGAL_COUNTRY_CODE = "PT";

    private static Map<String, GeoArea> pointsOfInterest = new HashMap();

    private Intent locationServiceIntent;
    private BackgroundService backgroundService;
    private LocationHandlerService locationHandlerService;

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

        // check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "É preciso aceitar...", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,1);
            return;
        }

        locationHandlerService = new LocationHandlerService(
                this,
                MIN_TIME_LOCATION_UPDATE,
                MIN_DISTANCE_LOCATION_UPDATE
        );

        setLocationManager();

        if (!LocationHandlerService.hasLocationPermissionsAndConnection(this)) {
            LocationHandlerService.connectToGooglePlayServices(this);
        }

        backgroundService = new BackgroundService();
        locationServiceIntent = new Intent(this, backgroundService.getClass());

        if (!isMyServiceRunning(backgroundService.getClass())) {
            startService(locationServiceIntent);
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

        if (locationHandlerService != null) setLocationManager();
    }

    @Override
    protected void onPause() {
        if (locationHandlerService != null) locationHandlerService.stopLocationUpdates();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        stopService(locationServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    private void setLocationManager()
    {
        locationHandlerService.initializeLocationManager
                (
                        new ILocationManagerLocationChanged()
                        {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onLocationChanged(Location location)
                            {
                                String country = null, locality = null;
                                try {
                                    Address address = LocationHandlerService.getLocationAddress(MainActivity.this, location.getLatitude(), location.getLongitude());
                                    if (address != null)
                                    {
                                        country = address.getCountryCode();
                                        locality = address.getLocality();
                                    }
                                } catch (IOException ignored) {}

                                NearestPoint nearest = LocationHandlerService.getNearestPoint(location, pointsOfInterest.values());
                                boolean isInside = nearest.getDistance() <= nearest.getGeoArea().getRadius();

                                txt_location.setText("<Localização Atual> \n" +
                                        "\t" + country + " \n" +
                                        "\t" + locality + " \n" +
                                        "\t" + location.getLatitude() + ", " + location.getLongitude() + " \n" +
                                        "\t" + location.getAccuracy() + " \n" +
                                        "\t" + location.getProvider() + " \n" +
                                        "<Geofence mais perto> \n" +
                                        "\t" + nearest.getDistance() + " \n" +
                                        "\t" + nearest.getGeoArea().getLatitude() + ", " + nearest.getGeoArea().getLongitude() + " \n" +
                                        "<Dentro da geofence?> \n" +
                                        "\t" + isInside
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
        points.put("BCP", new GeoArea(38.743919,-9.306373,10));

        return points;
    }
}
