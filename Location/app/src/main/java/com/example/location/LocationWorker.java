package com.example.location;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class LocationWorker extends Worker {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final String TAG = "Locaionr";

    public LocationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void startLocation(String filename) {
        init();

        String data = "time: " + Calendar.getInstance().getTime() +
                        "latitude: " + mCurrentLocation.getLatitude() +
                        "longitude: " + mCurrentLocation.getLongitude();

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    new FileOutputStream(filename)
            );
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }


    }

    private void stopLocation() {

    }

    @NonNull
    @Override
    public Worker.Result doWork() {

        Context applicationContext = getApplicationContext();


        String fileName = getInputData().getString("file");
        Boolean recordReady = getInputData().getBoolean("recordReady", false);

        if(recordReady) {
            try {
                startLocation(fileName);
                Log.d("start", "Success");
                return Result.success();
            } catch (Throwable throwable) {
                Log.e(TAG, "Error", throwable);
                return Result.failure();
            }
        } else {
            try {
                stopLocation();
                Log.d("stop", "Success");
                return Result.success();
            } catch (Throwable throwable) {
                Log.e(TAG, "Error", throwable);
                return Result.failure();
            }
        }
    }
}
