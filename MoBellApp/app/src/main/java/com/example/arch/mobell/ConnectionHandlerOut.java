package com.example.arch.mobell;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by jannik on 10/3/15.
 */
public class ConnectionHandlerOut implements Runnable {

    public static final int SAMPLING_RATE = 8000;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT);


    private final Socket clientSocket;
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);


        byte audioData[] = new byte[BUFFER_SIZE];
        AudioRecord recorder = new AudioRecord(AUDIO_SOURCE,
                SAMPLING_RATE, CHANNEL_IN_CONFIG,
                AUDIO_FORMAT, BUFFER_SIZE);
        recorder.startRecording();

        OutputStream os = null;
        try {
            os = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean mStop = false;
        while (!mStop) {
            int status = recorder.read(audioData, 0, audioData.length);

            if (status == AudioRecord.ERROR_INVALID_OPERATION ||
                    status == AudioRecord.ERROR_BAD_VALUE) {
                Log.e("asdf", "Error reading audio data!");
                return;
            }

            try {
                os.write(audioData, 0, audioData.length);
            } catch (IOException e) {
                Log.e("asdf", "Error saving recording ", e);
                return;
            }
        }

        try {
            os.close();

            recorder.stop();
            recorder.release();

            Log.v("asdf", "Recording done…");
            mStop = false;

        } catch (IOException e) {
            Log.e("asdf", "Error when releasing", e);
        }
        
    }
    ConnectionHandlerOut(Socket socket) {
        clientSocket = socket;
    }
}
