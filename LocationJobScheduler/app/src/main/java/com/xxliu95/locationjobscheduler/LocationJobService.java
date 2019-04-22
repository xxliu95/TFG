package com.xxliu95.locationjobscheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import static com.xxliu95.locationjobscheduler.MainActivity.fileName;

public class LocationJobService extends JobService
                                implements LocationListener {
    private static final String TAG = "LocationJobService";
    private boolean jobCancelled = false;
    private String loc = "";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");

        doWork(params);

        return true;
    }

    private void doWork(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    Log.d(TAG, "run: " + fileName);


                    final File file = new File(fileName);

                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                new FileOutputStream(fileName)
                        );
                        outputStreamWriter.append(loc);
                        outputStreamWriter.close();
                    }
                    catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }
                    if (jobCancelled) {
                        return;
                    }
                    try {
                        Thread.sleep( 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "run: finished");
                jobFinished(params, false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled");
        jobCancelled = true;
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        location.getLatitude();
        location.getLongitude();

        loc = "time: " + Calendar.getInstance().getTime() +
                "latitude: " +  location.getLatitude() +
                "longitude: " + location.getLongitude();
        Log.d(TAG, " "+loc);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
