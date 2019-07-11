package com.example.x190629.testes_geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import static android.content.Context.LOCATION_SERVICE;
import static android.util.Half.POSITIVE_INFINITY;

/**
 * Created by X190629 on 09/07/2019.
 */

public class LocationService
{
    private static final String PROVIDER_GPS = "gps";
    private static final String PROVIDER_NETWORK = "network";

    private final int minTimeUpdate, minDistanceUpdate;

    private Activity activity;
    private Context context;
    private LocationManager locManager;
    private LocationListener locListener;
    private Location locationGps = null, locationNetwork = null;
    private Location nearestGoal;

    public LocationService(@NonNull Activity activity, @NonNull Context context, int minTimeUpdate, int minDistanceUpdate)
    {
        this.activity = activity;
        this.context = context;
        this.minTimeUpdate = minTimeUpdate;
        this.minDistanceUpdate = minDistanceUpdate;
    }

    public void initializeLocationManager(@NonNull final ILocationManagerLocationChanged onLocationManagerLocationChanged,
                                          @NonNull final ILocationManagerProviderEnabled onLocationManagerProviderEnabled,
                                          @NonNull final ILocationManagerProviderDisabled onLocationManagerProviderDisabled)
    {
        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locListener = new LocationListener()
        {
            public void onLocationChanged(Location location)
            {
                switch (location.getProvider())
                {
                    case PROVIDER_GPS:
                        locationGps = location;
                        break;
                    case PROVIDER_NETWORK:
                        locationNetwork = location;
                        break;
                }

                onLocationManagerLocationChanged.onLocationChanged(location);
            }

            @Override
            public void onProviderEnabled(String s)
            {
                onLocationManagerProviderEnabled.onProviderEnabled(s);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderDisabled(String provider)
            {
                switch (provider)
                {
                    case PROVIDER_GPS:
                        locationGps = null;
                        break;
                    case PROVIDER_NETWORK:
                        locationNetwork = null;
                        break;
                }

                onLocationManagerProviderDisabled.onProviderDisabled(provider);
            }
        };

        startLocationUpdates();
    }

    public void startLocationUpdates()
    {
        startLocationUpdates(minTimeUpdate, minDistanceUpdate);
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(int minTimeUpdate, int minDistanceUpdate)
    {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                minTimeUpdate,
                minDistanceUpdate,
                locListener);

        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                minTimeUpdate,
                minDistanceUpdate,
                locListener);
    }

    public void stopLocationUpdates()
    {
        if (locManager != null && locListener != null) {
            locManager.removeUpdates(locListener);
        }
        locListener = null;
    }

    public Location getBestLocationAvailable()
    {
        if (locationGps != null) {
            return locationGps;
        }

        if (locationNetwork != null) {
            return locationNetwork;
        }

        return null;
    }

    public static boolean hasLocationPermissionsAndConnection(@NonNull Context context, @NonNull Activity activity)
    {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                isAnyLocationProviderConnected(context);
    }

    public static boolean isAnyLocationProviderConnected(@NonNull Context context)
    {
        return isGpsLocationConnected(context) || isNetworkLocationConnected(context);
    }

    public static boolean isGpsLocationConnected(@NonNull Context context)
    {
        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return service != null &&
                service.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                isInHighPrecisionMode(context);
    }

    public static boolean isInHighPrecisionMode(@NonNull Context context)
    {
        int locationMode;
        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(),Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
        return locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
    }

    public static boolean isNetworkLocationConnected(@NonNull Context context)
    {
        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return service != null && service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static float getDistanceBetweenLocations(double startLatitude, double startLongitude,
                                                    double endLatitude, double endLongitude)
    {
        // calculate distance (min and max)
        float[] distance = new float[2];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, distance);
        return distance[0];
    }

    public static float getDistanceBetweenLocations(Location startLocation, Location endLocation)
    {
        return getDistanceBetweenLocations(startLocation.getLatitude(), startLocation.getLongitude(),
                endLocation.getLatitude(), endLocation.getLongitude());
    }

    private Location getNearestPoint(Location currentLocation, Location[] pointsOfInterest){
    nearestGoal = null;
    float minDistance = POSITIVE_INFINITY;
        for(Location location : pointsOfInterest){
        if(minDistance > getDistanceBetweenLocations(location,currentLocation)) {
            minDistance = getDistanceBetweenLocations(location, currentLocation);
            nearestGoal = location;
        }
    }

    return nearestGoal;
    }

}
