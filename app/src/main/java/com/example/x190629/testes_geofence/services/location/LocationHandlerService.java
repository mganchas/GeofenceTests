package com.example.x190629.testes_geofence.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerLocationChanged;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerProviderDisabled;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerProviderEnabled;
import com.example.x190629.testes_geofence.entities.GeoArea;
import com.example.x190629.testes_geofence.entities.NearestPoint;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;
import static java.lang.Float.POSITIVE_INFINITY;

public class LocationHandlerService
{
    private static final String TAG = LocationHandlerService.class.getSimpleName();
    private static final String PROVIDER_GPS = "gps";
    private static final String PROVIDER_NETWORK = "network";
    private static final int INTERVAL_TIME_LOCATION_UPDATE = 10 * 1000; // in milliseconds
    private static final int INTERVAL_TIME_FASTEST_LOCATION_UPDATE = 5 * 1000; // in milliseconds

    private final int minTimeUpdate, minDistanceUpdate;

    private Context context;
    private LocationManager locManager;
    private LocationListener locListener;
    private Location locationGps = null, locationNetwork = null;

    public LocationHandlerService(@NonNull Context context, int minTimeUpdate, int minDistanceUpdate)
    {
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

        try {
            startLocationUpdates();
        } catch (IllegalAccessException e)
        {
            Toast.makeText(context, "No Location permissions", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void startLocationUpdates() throws IllegalAccessException
    {
        startLocationUpdates(minTimeUpdate, minDistanceUpdate);
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(int minTimeUpdate, int minDistanceUpdate) throws IllegalAccessException
    {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            throw new IllegalAccessException("No permission for Location");
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

    @SuppressLint("MissingPermission")
    public Location getGetBestLocationAvailable()
    {
        if (locationGps != null) {
            return locationGps;
        }

        if (locationNetwork != null) {
            return locationNetwork;
        }

        Location ctxLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (ctxLocation != null) {
            return ctxLocation;
        }

        ctxLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (ctxLocation != null) {
            return ctxLocation;
        }

        ctxLocation = locManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (ctxLocation != null) {
            return ctxLocation;
        }

        return null;
    }

    public static boolean hasLocationPermissionsAndConnection(@NonNull Context context)
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

    public static Address getLocationAddress(Context context, double latitude, double longitude) throws IOException
    {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        if (addresses != null && !addresses.isEmpty()) {
            return addresses.get(0);
        }
        return null;
    }

    public static NearestPoint getNearestPoint(Location currentLocation, Collection<GeoArea> pointsOfInterest)
    {
        GeoArea nearestGoal = null;
        float distance = POSITIVE_INFINITY;

        for(GeoArea location : pointsOfInterest)
        {
            float distanceBetweenLocations = getDistanceBetweenLocations(
                    location.getLatitude(),
                    location.getLongitude(),
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );

            if(distance > distanceBetweenLocations)
            {
                distance = distanceBetweenLocations;
                nearestGoal = location;
            }
        }

        return new NearestPoint(nearestGoal, distance);
    }

    public static void connectToGooglePlayServices(@NonNull final Activity activity)
    {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL_TIME_LOCATION_UPDATE);
        locationRequest.setFastestInterval(INTERVAL_TIME_FASTEST_LOCATION_UPDATE);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>()
        {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                } catch (ApiException exception) {
                    if (exception.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(activity, 100);
                        } catch (IntentSender.SendIntentException e) {
                            Log.d(TAG, e.getMessage());
                        } catch (ClassCastException e) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }
            }
        });
    }
}
