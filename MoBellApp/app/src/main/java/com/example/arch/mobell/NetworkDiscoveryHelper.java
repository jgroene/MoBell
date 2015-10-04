package com.example.arch.mobell;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jannik on 10/3/15.
 */
public class NetworkDiscoveryHelper extends Thread {
    P2pService mSevice;
    List ipAddresses;
    InetAddress hostIp;
    boolean isHost;
    NetworkDiscoveryHelper(P2pService service, InetAddress host) {
        mSevice = service;
        hostIp = host;
        isHost = false;
        ipAddresses = new ArrayList();
    }
    NetworkDiscoveryHelper(P2pService service) {
        mSevice = service;
        isHost = true;
        ipAddresses = new ArrayList();
    }
    @Override
    public void run() {
        Log.e("asdf", "ndiscoveryhelper starterd");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.e("asdf", "foo", e);
        }
        if (isHost) {Log.e("asdf","were host");
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(1234);
            } catch (IOException e) {
                Log.e("asdf", "foo", e);
            }
            long now = System.currentTimeMillis();
            List clients = new ArrayList();
            String myIp = "";
            while (System.currentTimeMillis() - now < 3000) {
                Socket client = null;
                try {
                    client = serverSocket.accept();
                } catch (IOException e) {
                    Log.e("asdf", "foo", e);
                }
                BufferedReader ois = null;
                try {
                    ois = new BufferedReader(new InputStreamReader(client.getInputStream()));
                } catch (IOException e) {
                    Log.e("asdf", "foo", e);
                }
                String handshake_data = null;
                try {
                    Log.e("asdf", "trying to read");
                    Thread.sleep(2000);
                    handshake_data = ois.readLine();
                } catch (OptionalDataException e) {
                    Log.e("asdf", "foo", e);
                } catch (IOException e) {
                    Log.e("asdf", "foo", e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String handshake[] = handshake_data.split(";");
                Log.e("asdf", handshake_data);
                if (handshake[0].equals("MoBellHandshake")) {
                    myIp = handshake[1];
                    ipAddresses.add(client.getInetAddress());
                    clients.add(client);
                }
                try {
                    ois.close();
                } catch (IOException e) {
                    Log.e("asdf", "foo", e);
                }
            }
            try {
                ipAddresses.add(InetAddress.getByName(myIp));
            } catch (UnknownHostException e) {
                Log.e("asdf", "foo", e);
            }
            for (Object client : clients) {
                BufferedWriter oos = null;
                try {
                    oos = new BufferedWriter(new OutputStreamWriter(((Socket) client).getOutputStream()));
                } catch (IOException e) {
                    Log.e("asdf", "foo", e);
                }
                try {
                    String ipString = "";
                    for (Object address : ipAddresses) {
                        ipString += ((InetAddress) address).toString() + ";";
                    }
                    ipString += "\n";
                    oos.write(ipString);
                } catch (IOException e) {
                    Log.e("asdf", "foo", e);
                }
                try {
                    oos.close();
                } catch (IOException e) {
                    Log.e("asdf", "foo", e);
                }
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                
            }
        } else {
            Log.e("asdf", "were client");
            Socket socket = null;
            try {
                socket = new Socket();
                socket.setReuseAddress(true);
                Log.e("asdf", "trying to connect... NOW");
                socket.connect(new InetSocketAddress(hostIp, 1234), 200);
            } catch (Exception ex) {
                Log.e("NETWORKH", "fuu", ex);
            }
            while(!socket.isConnected()) {
                try {
                    Log.e("asdf", "not yet connected");
                    Thread.sleep(100);
                    socket.connect(new InetSocketAddress(hostIp, 1234), 200);
                } catch (Exception eeeeeee) {
                }
            }
            try {
                Log.e("asdf", "connected");
                BufferedWriter oos = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Log.e("asdf", "wait...");
                Thread.sleep(2000);
                Log.e("asdf", "write!");
                oos.write("MoBellHandshake;"+hostIp.toString() + "\n");
                BufferedReader ois = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.e("asdf", "now were gonna read...");
                String ipString = ois.readLine();
                Log.e("asdf", "SUCCESSS! we read"+ipString);
                String ips[] = ipString.split(";");
                for (String str : ips) {
                    if (!str.equals("")) {
                        ipAddresses.add(InetAddress.getByName(str));
                    }
                }
                ois.close();
                oos.close();
                socket.close();
            } catch (Exception ex) {
                Log.e("NETWORKH", "fuu", ex);
            }
        }
        mSevice.initAudio(ipAddresses);
    }

}
