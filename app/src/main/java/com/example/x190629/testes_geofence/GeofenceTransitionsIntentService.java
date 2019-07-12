package com.example.x190629.testes_geofence;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by X190629 on 09/07/2019.
 */

public class GeofenceTransitionsIntentService extends IntentService
{
    Handler mHandler = new Handler();

    public GeofenceTransitionsIntentService(){
        super("GeofenceTransitionsIntentService");
    }

    // ...
    protected void onHandleIntent(Intent intent)
    {
        mHandler.post(new DisplayToast(TestesGeofenceApp.getContext(), "onHandleIntent started"));

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError())
        {
            mHandler.post(new DisplayToast(TestesGeofenceApp.getContext(), "geofencingEvent.hasError() = true"));
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        mHandler.post(new DisplayToast(TestesGeofenceApp.getContext(), "geofenceTransition: " + geofenceTransition));

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
        {
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence geofence : triggeringGeofences) {
                mHandler.post(new DisplayToast(TestesGeofenceApp.getContext(), "geofence.getRequestId(): " + geofence.getRequestId()));
            }

            // Get the transition details as a String.
            /*String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );*/
            String geofenceTransitionDetails = "";

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        }
        else
        {
            // Log the error.
            mHandler.post(new DisplayToast(TestesGeofenceApp.getContext(), "Hello World!"));
        }
    }
}