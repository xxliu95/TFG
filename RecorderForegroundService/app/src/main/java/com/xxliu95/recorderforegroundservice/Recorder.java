package com.xxliu95.recorderforegroundservice;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Clase Recorder con las útiles para grabar voz
 */
public class Recorder {
    private MediaRecorder recorder = null;

    private static Recorder instance = null;

    public Recorder() {}

    /**
     * Singleton
     *
     * @return la instancia
     */
    public static Recorder getInstance() {
        if (instance == null)
                instance = new Recorder();
        return instance;
    }

    /**
     * Graba y guarda el archivo en fileName
     *
     * @param fileName
     */
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

    /**
     * Para de grabar
     */
    protected void stopRecording() {
        if(recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }
}
