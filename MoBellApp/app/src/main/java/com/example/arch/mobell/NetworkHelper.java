package com.example.arch.mobell;

import android.os.Handler;
import android.util.Log;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by jannik on 10/3/15.
 */
public class NetworkHelper {
    public static int size;
    static List<InetAddress> addresses;
    static List<InetAddress> getAddresses() {
        return addresses;
    }
    static void addAddress(InetAddress addr) {
        addresses.add(addr);
    }
    static void setAddresses(List<InetAddress> addrs) {
        addresses = addrs;
    }
    static int distributed = 0;
    static void increaseDistributed() {distributed++;}
    static void setSize(int s) { size = s; };
    public static void broadcastIp() {
        Log.e("NETWORKHANDLER", "Started socket");
        try {
            ServerSocket serverSocket = new ServerSocket(8876);
            Log.e("NETWORKH", "" + size);
            for(int i = 0; i < size; i++) {
                Socket client = serverSocket.accept();
                Runnable run  = new NetworkHelperHandler(client);
                Handler handler = new Handler();
                handler.post(run);
            }
            while (distributed != size) {}
            serverSocket.close();
        } catch (Exception ex) {
            Log.e("NETWORKHANDLER", "error", ex);
            //ex.printStackTrace();

        }
    }
    public static void broadcastIp(InetAddress hostAddress) {

        Log.e("NETWORKHANDLER", "Started socket client");
        try {
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(hostAddress, 8876));
            while (!socket.isConnected());
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject("MoBellHandshake");
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Object object = ois.readObject();
            if(object.getClass() == List.class) {
                addresses = (List<InetAddress>) object;
                Log.e("NETWORKHANDLER", "received ips");
            }
            ois.close();
            oos.close();
            socket.close();
        } catch (Exception ex) {
            //TODO
        }
    }
}
