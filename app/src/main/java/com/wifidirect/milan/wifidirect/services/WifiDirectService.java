package com.wifidirect.milan.wifidirect.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.wifidirect.milan.wifidirect.Events;
import com.wifidirect.milan.wifidirect.WiFiDirectConstants;
import com.wifidirect.milan.wifidirect.WifiDirectApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by milan on 25.11.15..
 */
public class WifiDirectService extends Service implements WifiP2pManager.ChannelListener
        , WifiP2pManager.ActionListener, WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener{

    /** Binder. */
    private IBinder binder = new ServiceBinder();
    private IntentFilter intentFilter;
    public List<WifiP2pDevice> devicesList;
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
        // channel
        channel = manager.initialize(getApplicationContext(), Looper.getMainLooper(), null);
        // register reciver
        reciver = new WifiP2PReciver(manager, channel, this);

        devicesList = new ArrayList<>();

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



    /** Create binder. */
    public class ServiceBinder extends Binder {
        public WifiDirectService getService() {
            return WifiDirectService.this;
        }
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }




    @Override
    public void onSuccess() {
        // descovering peers
        /*
        if(manager != null){
            manager.discoverPeers(channel, this);
        }*/
    }


    @Override
    public void onFailure(int reason) {

    }


    @Override
    public void onChannelDisconnected() {

    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // InetAddress from WifiP2pInfo struct.
        //InetAddress groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

        if(info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
        } else if(info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
        }

    }

    public void conncetToDevice(WifiP2pDevice device){
        WifiP2pDevice wifiP2pDevice = device;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiP2pDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        // clean devices list
        if(!devicesList.isEmpty()) {
            devicesList.clear();
        }
        // add all peers to deviceslist
        devicesList.addAll(peers.getDeviceList());

    }



    /** Broadcast reciver. */
    private class WifiP2PReciver extends BroadcastReceiver{
        private WifiP2pManager manager;
        private WifiP2pManager.Channel channel;
        private Context mActivity;
        private WifiP2pManager.PeerListListener peerListListener;

        public WifiP2PReciver(WifiP2pManager manager, WifiP2pManager.Channel channel
                , WifiP2pManager.PeerListListener peerListListener){
            this.manager = manager;
            this.channel = channel;
            this.peerListListener = peerListListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // enable
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_WIFI_ENABLE);
                } else {
                    // disable
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_WIFI_DISABLE);
                }

            } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that the state of Wi-Fi p2p connectivity has changed.
                //sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_CONNECTION_CHANGED);
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if(networkInfo.isConnected()) {
                    // We are connected with the other device, request connection
                    // info to find group owner IP
                    //manager.requestConnectionInfo(channel, connectionListener);
                }


            } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that the available peer list has changed.
                sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_PEERS_LIST);
                if(manager != null){
                    manager.requestPeers(channel, peerListListener);
                }


            } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that this device details have changed.


            } else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that peer discovery has either started or stopped.

                int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);

                if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                    // started
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_DISCOVERY_STARTED);
                } else {
                    // stopped
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_DISCOVERY_STOPPED);
                }

            }

        }
    }



    /**
     * Send broadcast to fragment using Otto lib.
     * @param state String */
    private void sendBroadcastToActivity(final String state) {
        Events.WifiState wifiState = new Events.WifiState();
        wifiState.state = state;
        wifiState.value = "nesto";
        WifiDirectApplication.getBus().post(wifiState)
        ;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(reciver);
    }

}
