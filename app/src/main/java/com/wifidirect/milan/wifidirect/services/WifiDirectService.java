package com.wifidirect.milan.wifidirect.services;

import android.app.Activity;
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
import android.util.Log;

import com.wifidirect.milan.wifidirect.Events;
import com.wifidirect.milan.wifidirect.WiFiDirectConstants;
import com.wifidirect.milan.wifidirect.WifiDirectApplication;
import com.wifidirect.milan.wifidirect.connections.ChatClientAsyncTask;
import com.wifidirect.milan.wifidirect.connections.ChatServerAsyncTask;
import com.wifidirect.milan.wifidirect.connections.WifiClient;
import com.wifidirect.milan.wifidirect.connections.WifiServer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by milan on 25.11.15..
 */
public class WifiDirectService extends Service implements WifiP2pManager.ChannelListener
        , WifiP2pManager.ActionListener, WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener{
    private static final String TAG = "WifiService";
    /** Binder. */
    private IBinder mBinder = new ServiceBinder();
    private IntentFilter intentFilter;
    public List<WifiP2pDevice> mDevicesList;
    private WifiP2PReciver mReciver;
    public WifiP2pManager mManager;
    public WifiP2pManager.Channel mChannel;
    public InetAddress mAddress;

    @Override
    public void onCreate() {
        super.onCreate();

        intentFilter = new IntentFilter();
        initIntentFilter();

        // wifi manager
        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);

        // channel
        mChannel = mManager.initialize(getApplicationContext(), Looper.getMainLooper(), null);

        // register reciver
        mReciver = new WifiP2PReciver(mManager, mChannel, this, this);

        mDevicesList = new ArrayList<>();
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
        registerReceiver(mReciver, intentFilter);
        // descovering peers
        if(mManager != null){
            mManager.discoverPeers(mChannel, this);
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
        return mBinder;
    }


    @Override
    public void onSuccess() {
        Log.d(TAG, "Success");
    }


    @Override
    public void onFailure(int reason) {
        Log.e(TAG, "Failure" + String.valueOf(reason));
        // 0 - Indicates that the operation failed due to an internal error.
        // 1 - Indicates that the operation failed because p2p is unsupported on the device.
        // 2 - Indicates that the operation failed because the framework is busy and unable to service the request
    }


    @Override
    public void onChannelDisconnected() {

    }


    /** Refresh peers list. */
    public void refreshList() {
        mManager.discoverPeers(mChannel, this);
    }


    public void sendMessage() {
        // new ChatServerAsyncTask(getApplicationContext()).execute();
        new ChatClientAsyncTask(getApplicationContext(), mAddress.getHostAddress()).execute();
        Log.e(TAG, mAddress.getHostAddress());
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // InetAddress from WifiP2pInfo struct.
        Log.e(TAG, "ConnectionInfoAvailable");
        mAddress = info.groupOwnerAddress;
        if(info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
            Log.e(TAG, "GroupFormed, isGroupeOwner");
            sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_INFO_GROUP_FORMED_OWNER, null);

            new ChatServerAsyncTask(getApplicationContext()).execute();
        } else if(info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
            Log.e(TAG, "Only groupFormed");
            sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_INFO_GROUP_FORMED_CLIENT, null);
            new ChatClientAsyncTask(getApplicationContext(), info.groupOwnerAddress.toString()).execute();
        }

    }


    /** Esablish connection with another device.
     * @param device WifiP2pDevice. */
    public void connectToDevice(WifiP2pDevice device){
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, this);
    }


    /** Remove conncetion. */
    public void removeFromDevice() {
        mManager.removeGroup(mChannel, this);
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        // clean devices list
        if(!mDevicesList.isEmpty()) {
            mDevicesList.clear();
        }
        // add all peers to deviceslist
        mDevicesList.addAll(peers.getDeviceList());

    }



    /** Broadcast reciver. */
    private class WifiP2PReciver extends BroadcastReceiver{
        private WifiP2pManager manager;
        private WifiP2pManager.Channel channel;
        private WifiP2pManager.PeerListListener peerListListener;
        private WifiP2pManager.ConnectionInfoListener connectionInfoListener;

        public WifiP2PReciver(WifiP2pManager manager, WifiP2pManager.Channel channel
                , WifiP2pManager.PeerListListener peerListListener
                , WifiP2pManager.ConnectionInfoListener connectionInfoListener){
            this.manager = manager;
            this.channel = channel;
            this.peerListListener = peerListListener;
            this.connectionInfoListener = connectionInfoListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled.
                Log.e(TAG, "STATE CHANGED");
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // enable
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_WIFI_ENABLE, null);
                } else {
                    // disable
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_WIFI_DISABLE, null);
                }

            } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that the state of Wi-Fi p2p connectivity has changed.
                //sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_CONNECTION_CHANGED);
                Log.e(TAG, "P2P CONNECTION CHANGED");
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if(networkInfo.isConnected()) {
                    // We are connected with the other device, request connection
                    // info to find group owner IP
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_IS_CONNECTED, networkInfo.getExtraInfo());
                    manager.requestConnectionInfo(channel, connectionInfoListener);
                }


            } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that the available peer list has changed.
                Log.e(TAG, "P2P PEERS CHANGED");
                sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_PEERS_LIST, null);
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
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_DISCOVERY_STARTED, null);
                } else {
                    // stopped
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_DISCOVERY_STOPPED, null);
                }

            }

        }
    }





    /**
     * Send broadcast to fragment using Otto lib.
     * @param state String */
    private void sendBroadcastToActivity(final String state, String value) {
        Events.WifiState wifiState = new Events.WifiState();
        wifiState.state = state;
        wifiState.value = value;
        WifiDirectApplication.getBus().post(wifiState)
        ;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReciver);
    }

}
