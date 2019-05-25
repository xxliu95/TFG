package com.xxliu95.locationworkmanager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

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

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.xxliu95.locationworkmanager.MainActivity.TAG;

public class LocationWorker extends Worker {

    private static final int REQUEST_PERMISSION = 101;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static String fileName = null;

    public LocationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @NonNull
    @Override
    public Result doWork() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        Log.d(TAG, "doWork: getLocation");
        getLocation();

        return Result.success();
    }

    private void setLocationRequest() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        setResult(location);
                        Log.d(TAG,"locationCallback(); latitud: " + location.getLatitude() + " longitud: " + location.getLongitude() ) ;
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }

    }

    private void stopLocationRequest() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getApplicationContext().getMainExecutor(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            setResult(location);
                            Log.d(TAG,"getLastLocation(); latitud: " + location.getLatitude() + " longitud: " + location.getLongitude() ) ;
                        }
                    }
                });
            }
        }
    }

    private void setResult(Location location) {
        fileName = getApplicationContext().getExternalCacheDir().getAbsolutePath();
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
