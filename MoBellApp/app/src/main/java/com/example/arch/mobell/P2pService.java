package com.example.arch.mobell;

/**
 * Created by jannik on 10/3/15.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class P2pService extends Service {

    public static enum Broadcasts {
        // broadcast before the session starts
        onStatusChanged,
        onDeviceFound,
        onDeviceLost,
        onStartSession,
        onAbort,
        // broadcasts after the session starts
        onDeviceDisconnected,
        getDetails
    };

    boolean isInitialized = false;

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;
    boolean isWifiP2pEnabled = false;
    WifiP2PBroadcastReceiver receiver;
    private List peers = new ArrayList();
    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            peers.clear();
            peers.addAll(peerList.getDeviceList());
            for (Object device : peers) {

            }
        }
    };
    final HashMap<String, String> buddies = new HashMap<String, String>();
    int NetworkSize;

    public WifiP2pManager.PeerListListener getPeerListListener() {
        return peerListListener;
    }
    WifiP2pDnsSdServiceRequest serviceRequest;
    String name = "";


    public P2pService() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
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
        if (intent.hasExtra("name")) {
            name = intent.getStringExtra("name");
        }
        Intents command = (Intents) intent.getSerializableExtra("message");
        if (command == Intents.abort) {
            stopSelf();
        } else if (command == Intents.startSession) {
            //NetworkHelper.size = intent.getStringArrayExtra("macs").length;
            createConnection(intent.getStringArrayExtra("macs"));
        } else if (command == Intents.requestDetails) {
            //TODO
        }
        if (!isInitialized) {initialize();}
        return Service.START_NOT_STICKY;
    }

    public void initialize() {
        isInitialized = true;
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        startRegistration();
        receiver = new WifiP2PBroadcastReceiver(mManager, mChannel,this);
        registerReceiver(receiver, intentFilter);
        discover();
        discoverService();
    }

    // example of sending a broadcast:
    //Intent i = new Intent("p2pservice");
    //i.putExtra("message", Broadcasts.onStatusChanged);
    //sendBroadcast(i);



    /*
    -----------------------------Broadcasts-----------------------------
     */

    public void sendDeviceFound(String name, String address) {
        Intent i = new Intent("p2pservice");
        i.putExtra("message", Broadcasts.onDeviceFound);
        i.putExtra("name", name);
        i.putExtra("mac",  address);
        sendBroadcast(i);
    }
    public void sendDeviceLost(String address, String name) {
        Intent i = new Intent("p2pservice");
        i.putExtra("message", Broadcasts.onDeviceLost);
        i.putExtra("name", name);
        i.putExtra("mac", address);
        sendBroadcast(i);
    }

    public void sendStartSession() {
        Intent i = new Intent("p2pservice");
        i.putExtra("message", Broadcasts.onStartSession);
        sendBroadcast(i);
    }

    public void sendDeviceDisconnected(String name, String address) {
        Intent i = new Intent("p2pservice");
        i.putExtra("message", Broadcasts.onDeviceDisconnected);
        i.putExtra("name", name);
        i.putExtra("mac", address);
        sendBroadcast(i);
    }

    public void sendDetails(String[] names, String[] addresses) {
        Intent i = new Intent("p2pservice");
        i.putExtra("message", Broadcasts.getDetails);
        i.putExtra("names", names);
        i.putExtra("macs", addresses);
        sendBroadcast(i);
    }

    /*
        ---------------Methods for establishing connection------------
    */


    public void fail(String reason) {
        Intent failure = new Intent("p2pservice");
        failure.putExtra("message", Broadcasts.onAbort);
        failure.putExtra("reason", reason);
        sendBroadcast(failure);
        stopSelf();
    }

    private void startRegistration() {
        Map record = new HashMap();
        record.put("listenport", String.valueOf(222));
        record.put("buddyname", name);
        record.put("available", "visible");

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("MoBellCommunication", "_presence._tcp", record);

        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int arg0) {
                fail("Could not register service.");
            }
        });
    }

    public void discover() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reasonCode) {
                fail("Peer discovery failed with error code: " + Integer.toString(reasonCode));
            }
        });

    }
    public void discoverService() {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable (String fullDomain, Map record, WifiP2pDevice device) {
                buddies.put(device.deviceAddress, (String) record.get("buddyname"));
            }
        };
        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {
                resourceType.deviceName = buddies.containsKey(resourceType.deviceAddress) ? buddies.get(resourceType.deviceAddress) : resourceType.deviceName;
                sendDeviceFound(resourceType.deviceName, resourceType.deviceAddress);
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int code) {
                fail("Service request could not be created. Error code: " + Integer.toString(code));
            }
        });
        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int code) {
                fail("Service discovery failed. Error code: " + Integer.toString(code));
            }
        });
    }
    public void createConnection(String[] peers) {

        for (String peer : peers) {

            final WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = peer;
            config.wps.setup = WpsInfo.PBC;

            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {
                    fail("Connection could not be created. Error Code: " + Integer.toString(reason));
                }
            });
        }
    }
}

