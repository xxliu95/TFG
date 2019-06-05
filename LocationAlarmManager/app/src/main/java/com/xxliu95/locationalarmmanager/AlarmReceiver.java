package com.xxliu95.locationalarmmanager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.xxliu95.locationalarmmanager.MainActivity.TAG;

public class AlarmReceiver extends BroadcastReceiver {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static String fileName = null;

    /**
     * Al recibir la alarm realiza tareas en segundo plano
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received");

        setupRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        setLocationRequest(context);
        getLocation(context);
    }

    /**
     * Inicializar el locationRequest
     */
    private void setupRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    /**
     * Inicializa el callback
     *
     * @param context
     */
    private void setLocationRequest(final Context context) {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        setResult(context, location);
                        Log.d(TAG,"locationCallback(); latitud: " + location.getLatitude() + " longitud: " + location.getLongitude() ) ;
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }

    }

    /**
     * Quita los location updates
     */
    private void stopLocationRequest() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Obtiene el última localización y llama a setResult para que lo guarde en un fichero
     *
     * @param context
     */
    private void getLocation(final Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(context.getMainExecutor(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            setResult(context, location);
                            Log.d(TAG,"getLastLocation(); latitud: " + location.getLatitude() + " longitud: " + location.getLongitude() ) ;
                        }
                    }
                });
            }
        }
    }

    /**
     * Recibe un localización y lo guarda en un fichero txt
     *
     * @param context
     * @param location
     */
    private void setResult(Context context, Location location) {
        fileName = context.getExternalCacheDir().getAbsolutePath();
        fileName += "/location_" + System.currentTimeMillis() + ".txt";

        String latitud = String.valueOf(location.getLatitude());
        String longitud = String.valueOf(location.getLongitude());

        Log.d(TAG, "setResult: fileName: " + fileName);

        File file = new File(fileName);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(("latitud: " + latitud + "\nlongitud: " + longitud).getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
