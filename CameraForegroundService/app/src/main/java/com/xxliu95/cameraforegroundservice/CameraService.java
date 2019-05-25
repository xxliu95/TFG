package com.xxliu95.cameraforegroundservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.SurfaceView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CameraService extends Service {

    public static final String CHANNEL_ID = "CameraServiceChannel";
    private static final String TAG = "CameraService";

    private static String fileName = null;

    private Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0); //Esto es para cuando pulsas la notificacion lanza un intent a MainActivity


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Taking picture")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(2, notification);

        //do work on background thread

        timer = new Timer();
        timer.schedule(new Task(), 0, 10000);

        return START_NOT_STICKY;

    }

    private class Task extends TimerTask {
        @Override
        public void run() {
            takePicture();
        }
    }

    private void takePicture() {
        SurfaceView surface = new SurfaceView(getApplicationContext());
        Log.d(TAG, "Opening the camera");
        Camera camera = Camera.open();
        try {
            camera.setPreviewTexture(new SurfaceTexture(10));
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        camera.takePicture(null, null, jpegCallback);
    }

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream outStream = null;
            try {
                fileName = getExternalCacheDir().getAbsolutePath();
                fileName += "/picture_" + System.currentTimeMillis() + ".jpg";
                outStream = new FileOutputStream(fileName);
                outStream.write(data);
                outStream.close();
                Log.d(TAG, "onPictureTaken: Picture taken in: " + fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Log.d(TAG, "Releasing camera");
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer.purge();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
