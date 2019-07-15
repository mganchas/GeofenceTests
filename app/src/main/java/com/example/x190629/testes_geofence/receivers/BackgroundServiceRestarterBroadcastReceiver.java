package com.example.x190629.testes_geofence.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.x190629.testes_geofence.services.backgroundservices.BackgroundService;

/**
 * Created by X190629 on 15/07/2019.
 */

public class BackgroundServiceRestarterBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = BackgroundServiceRestarterBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive()");
        context.startService(new Intent(context, BackgroundService.class));
    }
}