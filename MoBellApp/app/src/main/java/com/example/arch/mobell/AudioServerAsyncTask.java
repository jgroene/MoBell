package com.example.arch.mobell;

import android.os.AsyncTask;

import java.io.InputStream;
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
            Runnable connectionHandlerin = new ConnectionHandlerOut(clientSocket);
            Runnable connectionHandlerout = new ConnectionHandlerOut(clientSocket);
            new Thread(connectionHandlerout).start();
            new Thread(connectionHandlerin).start();
        } catch (Exception ex) {

        }
        return null;
    }
    @Override
    protected void onPostExecute(String result) {

    }
}
