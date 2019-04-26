package com.xxliu95.recorderforeground;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 200;

    private static final String KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT";
    private static final int MAX_NUMBER_REQUEST_PERMISSIONS = 1;

    private static final List<String> sPermissions = Arrays.asList(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    private int mPermissionRequestCount;

    private static String fileName = null;

    private Button recordButton;
    private Button stopButton;

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

        final Recorder rec = new Recorder();

        recordButton = findViewById(R.id.record);
        stopButton = findViewById(R.id.stop);
        stopButton.setEnabled(false);

        recordButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                fileName = getExternalCacheDir().getAbsolutePath();
                fileName += "/recorded_" + System.currentTimeMillis() + ".mp3";

                Log.d("file", fileName);

                Context context = getApplicationContext();
                CharSequence starting = "Recording";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, starting, duration);
                toast.show();

                recordButton.setEnabled(false);
                stopButton.setEnabled(true);

                rec.startRecording(fileName);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Context context = getApplicationContext();
                CharSequence starting = "Stopped";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, starting, duration);
                toast.show();

                recordButton.setEnabled(true);
                stopButton.setEnabled(false);

                rec.stopRecording();
            }
        });
    }

}