package com.example.x190629.testes_geofence.services.threads;

import android.support.annotation.NonNull;

/**
 * Created by X190629 on 17/07/2019.
 */

public class ThreadService
{
    public static void Run(@NonNull Runnable threadAction,
                           IThreadProcessing onThreadProcessing)
    {
        // initialize thread
        Thread backThread = new Thread(threadAction);
        backThread.start();

        // do stuff while thread is processing
        if (onThreadProcessing != null) {
            onThreadProcessing.onThreadProcessing();
        }

        try {
            backThread.join();
        } catch (InterruptedException ignored) { }
    }

    public static void Run(@NonNull Runnable threadAction)
    {
        Run(threadAction, null);
    }
}
