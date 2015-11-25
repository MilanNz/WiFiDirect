package com.wifidirect.milan.wifidirect.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by milan on 25.11.15..
 */
public class WifiDirectService extends Service implements WifiP2pManager.ChannelListener, WifiP2pManager.ActionListener, WifiP2pManager.PeerListListener{
    private IntentFilter intentFilter;
    private List<WifiP2pDevice> devicesList;
    private WifiP2PReciver reciver;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    @Override
    public void onCreate() {
        super.onCreate();

        intentFilter = new IntentFilter();
        initIntentFilter();

        // wifi manager
        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(getApplicationContext(), Looper.getMainLooper(), null);

        devicesList = new ArrayList<>();

        // register reciver
        reciver = new WifiP2PReciver(manager, channel);

    }


    /** Inicialize intent filter. */
    private void initIntentFilter(){
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // Indicating that peer discovery has either started or stopped.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(reciver, intentFilter);
        // descovering peers
        if(manager != null){
            manager.discoverPeers(channel, this);
        }

        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    @Override
    public void onSuccess() {

    }


    @Override
    public void onFailure(int reason) {

    }


    @Override
    public void onChannelDisconnected() {

    }



    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        // clean devices list
        if(!devicesList.isEmpty()) {
            devicesList.clear();
        }

        for(WifiP2pDevice device : peers.getDeviceList()){
            devicesList.add(device);
        }
    }



    /** Broadcast reciver. */
    private class WifiP2PReciver extends BroadcastReceiver{
        private WifiP2pManager manager;
        private WifiP2pManager.Channel channel;
        private Context mActivity;

        public WifiP2PReciver(WifiP2pManager manager, WifiP2pManager.Channel channel){
            this.manager = manager;
            this.channel = channel;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // disable
                } else {
                    // enable
                }

            } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            } else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {

            }

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(reciver);
    }





}
