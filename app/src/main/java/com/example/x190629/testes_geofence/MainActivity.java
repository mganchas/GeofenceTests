package com.example.x190629.testes_geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.x190629.testes_geofence.entities.NearestPoint;
import com.example.x190629.testes_geofence.entities.PointsOfInterest;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerLocationChanged;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerProviderDisabled;
import com.example.x190629.testes_geofence.services.abstractions.ILocationManagerProviderEnabled;
import com.example.x190629.testes_geofence.services.backgroundservices.BackgroundService;
import com.example.x190629.testes_geofence.services.location.LocationHandlerService;
import com.example.x190629.testes_geofence.services.notifications.NotificationService;
import com.example.x190629.testes_geofence.workers.LocationWorker;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {
    private static final int MIN_TIME_LOCATION_UPDATE = 0; // in milliseconds
    private static final int MIN_DISTANCE_LOCATION_UPDATE = 0; // in meters
    private static final String PORTUGAL_COUNTRY_CODE = "PT";

    private static final String TAG = MainActivity.class.getName();

    //private Intent locationServiceIntent;
    //private BackgroundService backgroundService;
    private LocationHandlerService locationHandlerService;

    private TextView txt_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_location = findViewById(R.id.txt_localizacao);

        findViewById(R.id.btn_add_bcp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"add bcp click");


               /* NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MainActivity.this,  getResources().getString(R.string.app_name));
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                /* Create or update. */
                 /*   NotificationChannel channel = new NotificationChannel(getResources().getString(R.string.app_name),
                            getResources().getString(R.string.app_name),
                            NotificationManager.IMPORTANCE_HIGH);

                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(channel);

                    }
                }

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    notificationBuilder
                            .setContentTitle(getString(R.string.app_name))  // We still need this because on old android versions bigtextstyle displays nothing
                            .setContentText("TEXTOOO")                        // We still need this because on old android versions bigtextstyle displays nothing
                            .setSmallIcon(R.drawable.ic_all_out_black_24dp)
                            .setChannelId(getResources().getString(R.string.app_name))
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setAutoCancel(true);

                }else{
                    if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 ) {

                        notificationBuilder
                                .setContentTitle(getString(R.string.app_name))  // We still need this because on old android versions bigtextstyle displays nothing
                                .setContentText("textoooo")                        // We still need this because on old android versions bigtextstyle displays nothing
                                .setSmallIcon(R.drawable.ic_all_out_black_24dp)
                                .setLargeIcon(BitmapFactory.decodeResource(  getApplicationContext().getResources(), R.drawable.ic_all_out_black_24dp))
                                .setChannelId(getResources().getString(R.string.app_name))
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setAutoCancel(true);
                    }else{
                        notificationBuilder
                                .setContentTitle(getString(R.string.app_name))  // We still need this because on old android versions bigtextstyle displays nothing
                                .setContentText("textooooo")                        // We still need this because on old android versions bigtextstyle displays nothing
                                .setSmallIcon(R.drawable.ic_all_out_black_24dp)
                                .setChannelId(getResources().getString(R.string.app_name))
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setAutoCancel(true);
                    }

                }

                if (notificationManager != null) {
                    notificationManager.notify(String.valueOf(System.currentTimeMillis()),1, notificationBuilder.build());
                }

*/

                PointsOfInterest.addBCP();
            }
        });

        findViewById(R.id.btn_remove_bcp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PointsOfInterest.removeBCP();
            }
        });

        findViewById(R.id.btn_getlocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                locationHandlerService = new LocationHandlerService(
                        MainActivity.this,
                        MIN_TIME_LOCATION_UPDATE,
                        MIN_DISTANCE_LOCATION_UPDATE
                );

                /*
                backgroundService = new BackgroundService();
                locationServiceIntent = new Intent(MainActivity.this, backgroundService.getClass());

                if (!isMyServiceRunning(backgroundService.getClass())) {
                    startService(locationServiceIntent);
                }
                */

                // check location permission
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "É preciso aceitar...", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }

                setLocationManager();

                if (!LocationHandlerService.hasLocationPermissionsAndConnection(MainActivity.this)) {
                    LocationHandlerService.connectToGooglePlayServices(MainActivity.this);
                }

                Constraints constraints = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();

                PeriodicWorkRequest saveRequest =
                        new PeriodicWorkRequest.Builder(LocationWorker.class, 15, TimeUnit.MINUTES)
                                .setConstraints(constraints)
                                .build();

                WorkManager.getInstance().enqueue(saveRequest);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        if (locationHandlerService != null) setLocationManager();
    }

    @Override
    protected void onPause() {
        if (locationHandlerService != null) locationHandlerService.stopLocationUpdates();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //stopService(locationServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    private void setLocationManager() {
        locationHandlerService.initializeLocationManager
        (
                new ILocationManagerLocationChanged() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onLocationChanged(Location location) {
                        String country = null, locality = null;
                        try {
                            Address address = LocationHandlerService.getLocationAddress(MainActivity.this, location.getLatitude(), location.getLongitude());
                            if (address != null) {
                                country = address.getCountryCode();
                                locality = address.getLocality();
                            }
                        } catch (IOException ignored) {
                        }

                        NearestPoint nearest = LocationHandlerService.getNearestPoint(location, PointsOfInterest.airports);
                        boolean isInside = nearest.getDistance() <= nearest.getGeoArea().getRadius();

                        txt_location.setText("<Localização Atual> \n" +
                                "\t" + country + " \n" +
                                "\t" + locality + " \n" +
                                "\t" + location.getLatitude() + ", " + location.getLongitude() + " \n" +
                                "\t" + location.getAccuracy() + " \n" +
                                "\t" + location.getProvider() + " \n" +
                                "<Geofence mais perto> \n" +
                                "\t" + nearest.getAirportName() + " \n" +
                                "\t" + nearest.getDistance() + " \n" +
                                "\t" + nearest.getGeoArea().getLatitude() + ", " + nearest.getGeoArea().getLongitude() + " \n" +
                                "<Dentro da geofence?> \n" +
                                "\t" + isInside
                        );

                    }
                },
                new ILocationManagerProviderEnabled() {
                    @Override
                    public void onProviderEnabled(String provider) {
                        setLocationManager();
                    }
                },
                new ILocationManagerProviderDisabled() {
                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }
        );
    }
}
