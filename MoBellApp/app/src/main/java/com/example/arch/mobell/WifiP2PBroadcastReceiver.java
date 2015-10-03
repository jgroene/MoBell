package com.example.arch.mobell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.InetAddress;

/**
 * Created by jannik on 10/3/15.
 */
public class WifiP2PBroadcastReceiver extends BroadcastReceiver {
    P2pService mService;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    boolean firstConnection=true;

    WifiP2pManager.ConnectionInfoListener connectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress;
            try {
                groupOwnerAddress = InetAddress.getByName(info.groupOwnerAddress.getHostAddress());
                if (info.groupFormed && info.isGroupOwner && firstConnection) {
                } else if (info.groupFormed && firstConnection) {
                    NetworkHelper.broadcastIp(groupOwnerAddress);
                }
                firstConnection = false;
            } catch (Exception ex) {
                //TODO
            }
        }
    };

    WifiP2PBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, P2pService activity) {
        super();
        mService = activity;
        mManager = manager;
        mChannel = channel;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

            } else {
                mService.fail("Wifi Direct is disabled.");
            }

        }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                mManager.requestPeers(mChannel, mService.getPeerListListener());
            }

        }else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                mManager.requestConnectionInfo(mChannel, connectionListener);
                mService.sendStartSession();
            } else {

            }

            //TODO: Implement other possible actions!
        }

    }
}
