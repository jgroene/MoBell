package com.example.arch.mobell;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jannik on 10/3/15.
 */
public class NetworkHelper {
    public static int size;
    static List addresses = new ArrayList();
    static List getAddresses() {
        return addresses;
    }
    static void addAddress(InetAddress addr) {
        addresses.add(addr);
    }
    static void setAddresses(List addrs) {
        addresses = addrs;
    }
    static int distributed = 0;
    static void increaseDistributed() {distributed++;}
    static void setSize(int s) { size = s; };
    public static void broadcastIp() {
        Log.e("NETWORKHANDLER", "Started socket");
        try {
            Thread tr = new Thread(new Runnable() {
                @Override
                public void run() {
                    ServerSocket serverSocket = null;
                    try {
                        serverSocket = new ServerSocket(8876);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("NETWORKH", "" + size);
                    for(int i = 0; i < size; i++) {
                        Socket client = null;
                        try {
                            client = serverSocket.accept();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        NetworkHelperHandler run  = new NetworkHelperHandler(client);
                        run.start();
                    }
                    while (distributed < size) {}
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            tr.start();
            tr.join();

        } catch (Exception ex) {
            Log.e("NETWORKHANDLER", "error", ex);
            //ex.printStackTrace();

        }
    }
    public static void broadcastIp(final InetAddress hostAddress) throws InterruptedException {

        Log.e("NETWORKHANDLER", "Started socket client");
        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.setReuseAddress(true);
                    socket.connect(new InetSocketAddress(hostAddress, 8876));
                    OutputStream os = socket.getOutputStream();
                    BufferedWriter oos = new BufferedWriter((new OutputStreamWriter(os)));
                    Log.e("NETWORKH", "sending handshake");
                    oos.write("MoBellHandshake\n");
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    Object object = ois.readObject();
                    if(object.getClass() == List.class) {
                        addresses = (List) object;
                        Log.e("NETWORKHANDLER", "received ips");
                    }
                    ois.close();
                    oos.close();
                    socket.close();
                } catch (Exception ex) {
                    Log.e("NETWORKH", "fuu", ex);
                }
            }
        });
        thr.start();
        thr.join();
    }
}
