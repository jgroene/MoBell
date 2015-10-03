package com.example.arch.mobell;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class P2PService extends Service {

    public static enum Broadcasts {
        // broadcast before the session starts
        onStatusChanged,
        onDeviceConneted,
        onDeviceDisconnected,
        onStartSession,
        onAbort,
        // broadcasts after the session starts
        onDeviceOutOfRange,
        getDetails
    };

    public P2PService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static enum Intents {
        empty,
        abort,
        startSession,
        requestDetails
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra("message", 0);
        return Service.START_NOT_STICKY;
    }

    // example of sending a broadcast:
    //Intent i = new Intent("p2pservice");
    //i.putExtra("message", Broadcasts.onStatusChanged);
    //sendBroadcast(i);
}
