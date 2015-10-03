package com.example.arch.mobell;

import java.net.Socket;

/**
 * Created by jannik on 10/3/15.
 */
public class ConnectionHandler implements Runnable {
    private final Socket clientSocket;
    public void run() {
        
    }
    ConnectionHandler(Socket socket) {
        clientSocket = socket;
    }
}
