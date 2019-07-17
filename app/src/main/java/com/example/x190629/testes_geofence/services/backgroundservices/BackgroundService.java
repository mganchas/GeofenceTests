package com.example.x190629.testes_geofence.services.backgroundservices;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.x190629.testes_geofence.R;
import com.example.x190629.testes_geofence.entities.NearestPoint;
import com.example.x190629.testes_geofence.entities.PointsOfInterest;
import com.example.x190629.testes_geofence.receivers.BackgroundServiceRestarterBroadcastReceiver;
import com.example.x190629.testes_geofence.services.location.LocationHandlerService;
import com.example.x190629.testes_geofence.services.notifications.NotificationService;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    private static final String TAG = BackgroundService.class.getSimpleName();
    private static final int DEFAULT_TIMER_DELAY = 30 * 1000;
    private Timer timer;
    private TimerTask timerTask;
    private boolean pushSent;

    public BackgroundService() { super(); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, TAG + ".onStartCommand()");

        startTimer();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, TAG + ".onDestroy()");

        Intent broadcastIntent = new Intent(getApplicationContext(), BackgroundServiceRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        stopTimerTask();
    }

    public void startTimer() {
        Log.d(TAG, TAG + ".startTimer()");

        initializeTimerTask();

        timer = new Timer();
        timer.schedule(timerTask, DEFAULT_TIMER_DELAY, DEFAULT_TIMER_DELAY);
    }

    public void initializeTimerTask() {
        Log.d(TAG, TAG + ".initializeTimerTask()");
        timerTask = new TimerTask() {
            public void run() {
                Log.d(TAG, TAG + ".initializeTimerTask().run()");

                if (!LocationHandlerService.hasLocationPermissionsAndConnection(BackgroundService.this))
                    return;
                else {
                    //get na bd
                    String country = null, locality = null;
                    LocationHandlerService ls = null;
                    Location location = null;
                    location = ls.getGetBestLocationAvailable();
                    try {

                boolean locationOn = LocationHandlerService.isAnyLocationProviderConnected(BackgroundService.this);
                Log.d(TAG, TAG + ".initializeTimerTask().run() locationOn = " + locationOn);

                //NotificationService.cancelAll(BackgroundService.this);
                NotificationService.sendNotification(BackgroundService.this,
                        locationOn ? "O utilizador ativou a localização" : "O utilizador desligou a localização",
                        "Serviços localização ligados: " + locationOn,
                        "Powered by Millennium BCP",
                        R.drawable.ic_launcher_foreground
                );


                // TODO: business logic para quando em background
                        location = ls.getGetBestLocationAvailable();
                        Address address = LocationHandlerService.getLocationAddress(BackgroundService.this, location.getLatitude(), location.getLongitude());
                        if (address != null) {
                            country = address.getCountryCode();
                        }
                    } catch (IOException ignored) {
                    }
                    NearestPoint nearest = LocationHandlerService.getNearestPoint(location, PointsOfInterest.pointsOfInterest.values());
                    boolean isInside = nearest.getDistance() <= nearest.getGeoArea().getRadius();
                    if (!isInside && country.equals("PT")) {
                        pushSent = false;
                        //upsert na bd
                    } else if (isInside && pushSent == false) {
                        //send push
                        pushSent = true;
                        //upsert na bd

                    } else if (!country.equals("PT") && pushSent == false) {
                        //send push
                        pushSent = true;
                        //upsert na bd

                    }


                }
            }
        };
    }


    public void stopTimerTask() {
        Log.d(TAG, TAG + ".stopTimerTask()");

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
