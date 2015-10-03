package com.example.arch.mobell;

import android.os.AsyncTask;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jannik on 10/3/15.
 */
public class AudioServerAsyncTask extends AsyncTask<InputStream, Void, String> {

    AudioServerAsyncTask(boolean host) {

    }

    @Override
    protected String doInBackground(InputStream... out) {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket clientSocket = serverSocket.accept();
            Runnable connectionHandler = new ConnectionHandler(clientSocket);
            new Thread(connectionHandler).start();
        } catch (Exception ex) {

        }
        return null;
    }
    @Override
    protected void onPostExecute(String result) {

    }
}
