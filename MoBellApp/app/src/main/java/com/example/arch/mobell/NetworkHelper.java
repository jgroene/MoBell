package com.example.arch.mobell;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jannik on 10/3/15.
 */
public class NetworkHelper {
    static String[] addresses;
    static String[] getAddresses() {
        return addresses;
    }
    public static void broadcastIp(InetAddress hostAddress, boolean host) {
        if (host) {
            try {
                ServerSocket serverSocket = new ServerSocket(8877);
                Socket client = serverSocket.accept();
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                Object object = ois.readObject();
                if (object.getClass() == String.class && (String) object == "MoBellHandshake") {
                    
                }
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
                oos.close();
                os.close();
                socket.close();
            } catch (Exception ex) {
                //TODO
            }
        }
    }
}
