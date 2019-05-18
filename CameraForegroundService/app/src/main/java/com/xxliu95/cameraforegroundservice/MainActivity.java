package com.xxliu95.cameraforegroundservice;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;
import java.util.List;

import static com.xxliu95.cameraforegroundservice.CameraService.CHANNEL_ID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 200;

    private static final String KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT";
    private static final int MAX_NUMBER_REQUEST_PERMISSIONS = 1;

    private static final List<String> sPermissions = Arrays.asList(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    private int mPermissionRequestCount;

    private Button buttonCapture;
    private static final String TAG = "CameraForeground";

    private static boolean started = false;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PERMISSIONS_REQUEST_COUNT, mPermissionRequestCount);
    }

    private void requestPermissionsIfNecessary() {
        if (!checkAllPermissions()) {
            if (mPermissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                mPermissionRequestCount += 1;
                ActivityCompat.requestPermissions(
                        this,
                        sPermissions.toArray(new String[0]),
                        REQUEST_CODE_PERMISSIONS);
            } else {
                finish();
            }
        }
    }

    private boolean checkAllPermissions() {
        boolean hasPermissions = true;
        for (String permission : sPermissions) {
            hasPermissions &=
                    ContextCompat.checkSelfPermission(
                            this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return hasPermissions;
    }

    /** Permission Checking **/

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary(); // no-op if permissions are granted already.
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mPermissionRequestCount =
                    savedInstanceState.getInt(KEY_PERMISSIONS_REQUEST_COUNT, 0);
        }

        requestPermissionsIfNecessary();

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        buttonCapture = (Button) findViewById(R.id.buttonCapture);

        if (!started) {
            buttonCapture.setText("Start");
        } else {
            buttonCapture.setText("Stop");
        }

        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!started) {
                    startService();
                    started = true;
                    buttonCapture.setText("Stop");
                } else {
                    stopService();
                    started = false;
                    buttonCapture.setText("Start");
                }
            }
        });

    }

    private void startService() {
        Intent serviceIntent = new Intent(this, CameraService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopService() {
        Intent serviceIntent = new Intent(this, CameraService.class);
        stopService(serviceIntent);
    }
}
