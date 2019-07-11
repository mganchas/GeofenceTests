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

@SuppressLint("NewApi")
public class GeofenceTransitionsIntentService extends IntentService
{

    Handler mHandler;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */



    public GeofenceTransitionsIntentService(String name) {
        super(name);
        mHandler = new Handler();
    }

    // ...
    protected void onHandleIntent(Intent intent)
    {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError())
        {
            mHandler.post(new DisplayToast(this, "geofencingEvent.hasError() = true"));
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        mHandler.post(new DisplayToast(this, "geofenceTransition: " + geofenceTransition));

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
        {
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence geofence : triggeringGeofences) {
                mHandler.post(new DisplayToast(this, "geofence.getRequestId(): " + geofence.getRequestId()));
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
            mHandler.post(new DisplayToast(this, "Hello World!"));
        }
    }
}