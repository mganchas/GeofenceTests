package com.example.x190629.testes_geofence;

import android.app.Application;
import android.content.Context;

/**
 * Created by X190629 on 09/07/2019.
 */

public class TestesGeofenceApp extends Application
{
    private static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        TestesGeofenceApp.context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
