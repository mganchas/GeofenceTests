package com.example.x190629.testes_geofence.services.backgroundservices;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.x190629.testes_geofence.receivers.BackgroundServiceRestarterBroadcastReceiver;

import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service
{
    private static final String TAG = BackgroundService.class.getSimpleName();
    private static final int DEFAULT_TIMER_DELAY = 1000;

    private Timer timer;
    private TimerTask timerTask;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand()");

        startTimer();

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        Intent broadcastIntent = new Intent(this, BackgroundServiceRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        stopTimerTask();
    }

    public void startTimer()
    {
        Log.d(TAG, "startTimer()");

        initializeTimerTask();

        timer = new Timer();
        timer.schedule(timerTask, DEFAULT_TIMER_DELAY, DEFAULT_TIMER_DELAY);
    }

    public void initializeTimerTask()
    {
        Log.d(TAG, "initializeTimerTask()");

        timerTask = new TimerTask()
        {
            public void run()
            {
                Log.d(TAG, "initializeTimerTask().run()");

                // TODO: meter aqui a business logic...

            }
        };
    }

    public void stopTimerTask()
    {
        Log.d(TAG, "stopTimerTask()");

        if (timer != null)
        {
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
