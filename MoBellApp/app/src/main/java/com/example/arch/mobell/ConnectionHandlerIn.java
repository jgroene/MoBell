package com.example.arch.mobell;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by jannik on 10/3/15.
 */

public class ConnectionHandlerIn implements Runnable {

    public static final int SAMPLING_RATE = 8000;
    public static final int CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int CHUNK_LENGTH = 1000;


    private final Socket clientSocket;
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        AudioTrack audioTrack = new  AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLING_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AUDIO_FORMAT, 500000, AudioTrack.MODE_STATIC);

        InputStream is = null;
        try {
            is = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[CHUNK_LENGTH];
        try {
            while(true) {
                //fill up
                int read = 0;
                while (read<CHUNK_LENGTH) {
                    int temp = is.read(buffer, 0, CHUNK_LENGTH-read);
                    read += temp;
                }

                audioTrack.write(buffer, 0, CHUNK_LENGTH);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    ConnectionHandlerIn(Socket socket) {
        clientSocket = socket;
    }
}
