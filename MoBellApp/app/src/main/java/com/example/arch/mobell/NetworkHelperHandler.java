package com.example.arch.mobell;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jannik on 10/3/15.
 */
public class NetworkHelperHandler extends Thread {
    Socket client;
    NetworkHelperHandler(Socket c) {
        client = c;
    }
    public final void run() {
        try {
            Log.e("NETWORKH", "WTFFFFFF");
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
            String handshake = (String) ois.readObject();
            //if (((String) object).equals("MoBellHandshake")) {
                NetworkHelper.addAddress(client.getInetAddress());
                Log.e("NETWORKHELPER", "Added address");
            //}
            while (NetworkHelper.getAddresses().size() < NetworkHelper.size) {
                Log.e("NETWORKH", "" + NetworkHelper.getAddresses().size());
            }
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
            oos.writeObject(NetworkHelper.getAddresses());
            Log.e("NETWORKHANDLER", "distributed addresses");
            NetworkHelper.increaseDistributed();
            oos.close();
            ois.close();
            client.close();
            } catch (Exception ex) {
                Log.e("NETWORKH",  "foo", ex);
            }

    }
}
