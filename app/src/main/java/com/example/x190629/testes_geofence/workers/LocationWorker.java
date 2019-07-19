package com.example.x190629.testes_geofence.workers;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.x190629.testes_geofence.R;
import com.example.x190629.testes_geofence.entities.NearestPoint;
import com.example.x190629.testes_geofence.entities.PointsOfInterest;
import com.example.x190629.testes_geofence.services.backgroundservices.BackgroundService;
import com.example.x190629.testes_geofence.services.location.LocationHandlerService;
import com.example.x190629.testes_geofence.services.notifications.NotificationService;

import java.io.IOException;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Created by X190629 on 18/07/2019.
 */

public class LocationWorker extends Worker
{
    private static final String TAG = LocationWorker.class.getSimpleName();

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        Log.i(TAG, "doWork()");

        BL();

        return Result.success();
    }

    private void BL()
    {
        //get na bd
        String country = null, locality = null;
        Location location = LocationHandlerService.getGetBestLocationAvailable(LocationWorker.this.getApplicationContext());

        Log.i(TAG, "BL() location is null? " + (location == null));
        if (location == null) {return;}

        try
        {
            Address address = LocationHandlerService.getLocationAddress(LocationWorker.this.getApplicationContext(), location.getLatitude(), location.getLongitude());
            if (address != null) {
                country = address.getCountryCode();
                locality = address.getLocality();
            }

        } catch (IOException ignored) {}

        Log.i(TAG, "BL() country is null? " + (country == null));
        if (country == null) { return; }

        NearestPoint nearest = LocationHandlerService.getNearestPoint(location, PointsOfInterest.airports);
        boolean isInside = nearest.getDistance() <= nearest.getGeoArea().getRadius();
        //send push
        NotificationService.cancelAll(LocationWorker.this.getApplicationContext());
        NotificationService.sendNotification(LocationWorker.this.getApplicationContext(),
                "Powered by Millennium BCP",
                "País: " + country,
                "Localidade: " + locality,
                R.drawable.ic_launcher_foreground
        );
    }
}