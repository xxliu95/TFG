package com.xxliu95.locationworkmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    private Button locationButton;

    private static final int REQUEST_PERMISSION = 101;

    public static final String TAG = "LocationWorkManager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationButton = (Button) findViewById(R.id.locationToggle);

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(getApplicationContext(), "Starting work", Toast.LENGTH_SHORT);
                toast.show();
                startWork();
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getApplicationContext(), "Esta app necesita permisos de GPS", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    public void startWork() {
        //PeriodicWorkRequest
        //Funciona cada 15 minutos minimo
        /*PeriodicWorkRequest locationWorkRequest = new PeriodicWorkRequest.Builder(
                LocationWorker.class,
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
        ).build();*/
        OneTimeWorkRequest locationWorkRequest = new OneTimeWorkRequest.Builder(LocationWorker.class)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build();
        Log.d(TAG, "startWork: enqueue work");
        WorkManager.getInstance().enqueue(locationWorkRequest);
    }
}
