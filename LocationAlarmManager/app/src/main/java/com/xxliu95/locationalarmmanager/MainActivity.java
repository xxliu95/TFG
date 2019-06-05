package com.xxliu95.locationalarmmanager;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private Button locationStart;

    private static final int REQUEST_PERMISSION = 101;

    public static final String TAG = "locationAlarmManager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationStart = findViewById(R.id.start);
        locationStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Sets a alarm a minute later
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 1);

                startAlarm(c);
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

    private void startAlarm(Calendar c) {
        String time = DateFormat.getTimeInstance(DateFormat.LONG).format(c.getTime());
        Log.d(TAG, "startAlarm: at " + time);

        Toast toast = Toast.makeText(this, "startAlarm: at " + time, Toast.LENGTH_SHORT);
        toast.show();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        alarmManager.setExact(AlarmManager.RTC, c.getTimeInMillis(), pendingIntent);
    }
}
