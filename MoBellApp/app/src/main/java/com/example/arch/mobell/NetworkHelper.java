package com.example.arch.mobell;

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
    public static void broadcastIp(InetAddress hostAddress, boolean host) {
        if (host) {
            try {
                ServerSocket serverSocket = new ServerSocket(8877);
                while (addresses.size() != size) {
                    Socket client = serverSocket.accept();
                    Runnable handler = new NetworkHelperHandler(client);
                }
                while (distributed != size) {}
                serverSocket.close();
            } catch (Exception ex) {
                //TODO
            }
        } else {
            try {
                Socket socket = new Socket();
                socket.setReuseAddress(true);
                socket.connect(new InetSocketAddress(hostAddress, 8877));
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeChars("MoBellHandshake");
                socket.setKeepAlive(true);
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Object object = ois.readObject();
                if(object.getClass() == List.class) {
                    addresses = (List<InetAddress>) object;
                }
                ois.close();
                oos.close();
                socket.close();
            } catch (Exception ex) {
                //TODO
            }
        }
    }
}
