package com.xxliu95.recorderforegroundservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import static com.xxliu95.recorderforegroundservice.ServiceChannel.CHANNEL_ID;

public class RecordService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String fileName = intent.getStringExtra("fileName");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0); //Esto es para cuando pulsas la notificacion lanza un intent a MainActivity

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("RecorderForegroundService")
                .setContentText("Grabando")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(2, notification);

        //do work on background thread
        Recorder rec = Recorder.getInstance();
        rec.startRecording(fileName);

        //stopSelf();

        return START_NOT_STICKY; //START_REDELIVER_INTENT mirar documentacion
    }

    @Override
    public void onDestroy() {
        Recorder rec = Recorder.getInstance();
        rec.stopRecording();
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
