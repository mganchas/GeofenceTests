package com.example.x190629.testes_geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import com.example.x190629.testes_geofence.GeofenceTransitionsIntentService;

/**
 * Created by X191104 on 7/12/2019.
 */

public class IntentReceiver extends BroadcastReceiver {


    /// <summary>
/// Start up the Intent Service to reverse geocode the lat/lon of the graffiti.
/// </summary>
    public IntentReceiver() {}

public  void onReceive(Context context, Intent intent){

        Intent intentReceived = new Intent(context, GeofenceTransitionsIntentService.class);
        intentReceived.putExtras(intent);
        context.startService(intentReceived);
    }


}