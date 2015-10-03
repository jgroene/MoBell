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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class P2pService extends Service {

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
                //((TextView) findViewById(R.id.textView)).append("\n" + ((WifiP2pDevice) device).deviceName);
            }
        }
    };
    final HashMap<String, String> buddies = new HashMap<String, String>();

    public WifiP2pManager.PeerListListener getPeerListListener() {
        return peerListListener;
    }
    WifiP2pDnsSdServiceRequest serviceRequest;
    String name = "";


    public P2pService() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        startRegistration();
        receiver = new WifiP2PBroadcastReceiver(mManager, mChannel,this);
        registerReceiver(receiver, intentFilter);
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
        int command = intent.getIntExtra("message", 0);
        return Service.START_NOT_STICKY;
    }

    // example of sending a broadcast:
    //Intent i = new Intent("p2pservice");
    //i.putExtra("message", Broadcasts.onStatusChanged);
    //sendBroadcast(i);


    /*
        ---------------Methods for establishing connection------------
    */

    private void startRegistration() {
        Map record = new HashMap();
        record.put("listenport", String.valueOf(222));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("MoBellCommunication", "_presence._tcp", record);

        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int arg0) {


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

            }
        });
        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
            }

            @Override
            public void onFailure(int code) {

            }
        });
    }
    public void createConnection() {

        for (Object peer : peers) {
            if (!(buddies.containsKey(((WifiP2pDevice)peer).deviceAddress))){continue;}
            WifiP2pDevice device = (WifiP2pDevice) peer;

            final WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;

            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {

                }
            });
        }
    }
}

