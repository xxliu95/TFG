package com.xxliu95.locationforegroundservice;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static com.xxliu95.locationforegroundservice.LocationService.CHANNEL_ID;

public class MainActivity extends AppCompatActivity {

    private Button locationToggle;
    private static boolean requesting = false;

    private static final int REQUEST_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
            Para versiones de Android mayor que OREO hay que crear un NotificationChannel para
            gestionar todas las notificaciones
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        locationToggle = (Button) findViewById(R.id.locationToggle);

        if (!requesting) {
            locationToggle.setText("Start");
        } else {
            locationToggle.setText("Stop");
        }

        locationToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!requesting) {
                    startService();
                    requesting = true;
                    locationToggle.setText("Stop");
                } else {
                    stopService();
                    requesting = false;
                    locationToggle.setText("Start");
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }
    }

    /**
     * Requerir permisos al usuario
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * Llama al servicio LocationService y empieza un Foreground Service
     */
    public void startService() {
        Intent serviceIntent = new Intent(this, LocationService.class);

        //startService(serviceIntent);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    /**
     * Para el Foreground Service
     */
    public void stopService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
    }

}
