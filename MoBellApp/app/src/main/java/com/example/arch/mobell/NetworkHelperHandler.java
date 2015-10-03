package com.example.arch.mobell;

import android.util.Log;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jannik on 10/3/15.
 */
public class NetworkHelperHandler implements Runnable {
    Socket client;
    NetworkHelperHandler(Socket c) {
        client = c;
    }
    public final void run() {
        try {
            Log.e("NETWORKH", "lkdshfjashldkfj");
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
            Object object = ois.readObject();
            if (object.getClass() == String.class && ((String) object).equals("MoBellHandshake")) {
                NetworkHelper.addAddress(client.getInetAddress());
                Log.e("NETWORKHELPER", "Added address");
            }
            while (NetworkHelper.getAddresses().size() != NetworkHelper.size) {
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

            }

    }
}
