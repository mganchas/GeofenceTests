package com.example.x190629.testes_geofence.factories;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

/**
 * Created by X190629 on 19/07/2019.
 */

public class BCPWorkerFactory
{
    public void create(Class<? extends ListenableWorker> workerType, int sleepMinutes, Constraints constraints)
    {
        PeriodicWorkRequest saveRequest = new PeriodicWorkRequest.Builder(workerType, sleepMinutes, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance().enqueueUniquePeriodicWork(workerType.getSimpleName(), ExistingPeriodicWorkPolicy.REPLACE, saveRequest);
    }
}
