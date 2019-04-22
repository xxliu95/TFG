package com.example.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 200;

    private static final String KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT";
    private static final int MAX_NUMBER_REQUEST_PERMISSIONS = 1;

    private static final List<String> sPermissions = Arrays.asList(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    private int mPermissionRequestCount;

    private static String fileName = null;

    private Button startButton;
    private Button stopButton;

    private WorkManager mWorkManager = WorkManager.getInstance();

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

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/location_" + System.currentTimeMillis() + ".txt";

        Log.d("file", fileName);

        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);
        stopButton.setEnabled(false);

        startButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Context context = getApplicationContext();
                CharSequence starting = "Start";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, starting, duration);
                toast.show();

                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                OneTimeWorkRequest request =
                        new OneTimeWorkRequest.Builder(LocationWorker.class)
                                .setInputData(createInputData())
                                .build();
                mWorkManager.enqueue(request);
            }
            private Data createInputData() {
                Data.Builder builder = new Data.Builder();
                builder.putBoolean("startReady", true);
                builder.putString("file", fileName);
                return builder.build();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Context context = getApplicationContext();
                CharSequence starting = "Stopped";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, starting, duration);
                toast.show();

                startButton.setEnabled(true);
                stopButton.setEnabled(false);

                OneTimeWorkRequest request =
                        new OneTimeWorkRequest.Builder(LocationWorker.class)
                                .setInputData(createInputData())
                                .build();
                mWorkManager.enqueue(request);
            }
            private Data createInputData() {
                Data.Builder builder = new Data.Builder();
                builder.putBoolean("startReady", false);
                return builder.build();
            }
        });
    }
}
