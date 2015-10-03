package com.example.hackzurich.wifidirecttestapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.example.hackzurich.wifidirecttestapp.MainActivity;

/**
 * Created by jannik on 10/3/15.
 */
public class WifiP2PBroadcastReceiver extends BroadcastReceiver {
    MainActivity mActivity;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    WifiP2PBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        super();
        mActivity = activity;
        mManager = manager;
        mChannel = channel;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mActivity.setWifiP2pEnabled(true);
            } else {
                mActivity.setWifiP2pEnabled(false);
            }

        }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, mActivity.getPeerListListener());
            }
        }
        //TODO: Implement other possible actions!
    }

}
