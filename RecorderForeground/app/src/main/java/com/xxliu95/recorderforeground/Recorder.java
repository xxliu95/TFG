package com.xxliu95.recorderforeground;


import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class Recorder {

    private MediaRecorder recorder = null;

    protected void startRecording(String fileName) {
        File file = new File(fileName);

        Log.d("Recorder", "startRecording: " + fileName);
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
            Log.e("Recorder", "prepare() failed");
        }
    }

    protected void stopRecording() {
        if(recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

}