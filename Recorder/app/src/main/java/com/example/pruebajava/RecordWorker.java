package com.example.pruebajava;

import android.content.Context;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class RecordWorker extends Worker {

    private MediaRecorder recorder = null;

    private static final String TAG = RecordWorker.class.getSimpleName();

    private void startRecording(String fileName) {
        File file = new File(fileName);

        if (recorder != null)
            recorder.release();
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setOutputFile(file.getAbsolutePath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            Log.e("PruebaJava", "prepare() failed");
        }
    }

    private void stopRecording() {
        if(recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    public RecordWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }


    @NonNull
    @Override
    public Worker.Result doWork() {

        Context applicationContext = getApplicationContext();

        String fileName = getInputData().getString("file");
        Boolean recordReady = getInputData().getBoolean("recordReady", false);

        if(recordReady) {
            try {
                startRecording(fileName);
                Log.d("start", "Success");
                return Result.success();
            } catch (Throwable throwable) {
                Log.e(TAG, "Error", throwable);
                return Result.failure();
            }
        } else {
            try {
                stopRecording();
                Log.d("stop", "Success");
                return Result.success();
            } catch (Throwable throwable) {
                Log.e(TAG, "Error", throwable);
                return Result.failure();
            }
        }
    }
}