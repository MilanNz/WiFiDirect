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
import android.util.Log;

import com.wifidirect.milan.wifidirect.Events;
import com.wifidirect.milan.wifidirect.listeners.MessageListener;
import com.wifidirect.milan.wifidirect.listeners.SocketListener;
import com.wifidirect.milan.wifidirect.connections.SocketTransfer;
import com.wifidirect.milan.wifidirect.connections.Transfer;
import com.wifidirect.milan.wifidirect.WiFiDirectConstants;
import com.wifidirect.milan.wifidirect.WifiDirectApplication;
import com.wifidirect.milan.wifidirect.notifications.WifiNotification;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by milan on 25.11.15..
 */
public class WifiDirectService extends Service implements WifiP2pManager.ChannelListener
        , WifiP2pManager.ActionListener, WifiP2pManager.PeerListListener
        , WifiP2pManager.ConnectionInfoListener, SocketListener {

    private static final String TAG = "WifiService";
    /** Server port. */
    private static final int PORT = 8888;
    /** Binder. */
    private IBinder mBinder = new ServiceBinder();
    private IntentFilter intentFilter;
    public List<WifiP2pDevice> mDevicesList;
    private WifiP2PReciver mReciver;
    public WifiP2pManager mManager;
    public WifiP2pManager.Channel mChannel;
    private Transfer mTransfer;
    private MessageListener mListener;


    @Override
    public void onCreate() {
        super.onCreate();

        intentFilter = new IntentFilter();
        // inicialize intent filter
        initIntentFilter();

        // wifi manager
        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);

        // channel
        mChannel = mManager.initialize(getApplicationContext(), Looper.getMainLooper(), null);

        // register reciver
        mReciver = new WifiP2PReciver(mManager, mChannel, this, this);

        // socket
        mTransfer = new SocketTransfer();

        // add listener
        mTransfer.addListener(this);
        mTransfer.addListener(this);


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
        if (mManager != null) {
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
        Log.e(TAG, "Success");
    }



    @Override
    public void onFailure(int reason) {
        Log.e(TAG, "Failure" + String.valueOf(reason));
        // 0 - Indicates that the operation failed due to an internal error.
        // 1 - Indicates that the operation failed because p2p is unsupported on the device.
        // 2 - Indicates that the operation failed because the framework is busy and unable
        // to service the request
    }


    @Override
    public void onChannelDisconnected() {
        Log.e(TAG, "channelDisconnected");
    }


    /** Refresh peers list. */
    public void refreshList() {
        mManager.discoverPeers(mChannel, this);
    }


    @Override
    public void onReceiver(String s) {
        Log.e(TAG, s);
        /*if (s.equals("receive")) {
            mListener.onConnected(true);
        }*/

        mListener.onMessageReceived(s);
        WifiNotification.createNotification(getApplicationContext(), s);
    }



    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.e(TAG, "ConnectionInfoAvailable");

        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
            Log.e(TAG, "GroupFormed, isGroupeOwner");

            // set port and start server
            mTransfer.setPort(PORT).startServer();
            // send message to client
            mTransfer.sendMessage("Hello from server!!!");

            sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_INFO_GROUP_FORMED_OWNER
                    , null);

        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
            Log.e(TAG, "GroupFormed");

            // set port, address and start client
            mTransfer.setPort(PORT).setAddress(info.groupOwnerAddress.getHostAddress())
                    .startClient();
            // send message to server
            mTransfer.sendMessage("Hello from client!");
        }

    }


    /** Send message to client or server.
     * @param message */
    public void sendMessage(String message) {
        mTransfer.sendMessage(message);
    }



    /** Esablish connection with another device.
     * @param device WifiP2pDevice. */
    public void connectToDevice(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;

        mManager.connect(mChannel, config, this);;
    }


    /** Remove conncetion. */
    public void removeConnection() {
        mManager.removeGroup(mChannel, this);
        mTransfer.closeConnection();
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        // clean devices list
        if (!mDevicesList.isEmpty()) {
            mDevicesList.clear();
        }
        // add all peers to deviceslist
        mDevicesList.addAll(peers.getDeviceList());
    }



    /** Broadcast reciver. */
    private class WifiP2PReciver extends BroadcastReceiver {
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

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled.
                Log.e(TAG, "STATE CHANGED");
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                // is wifi enabled
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_WIFI_ENABLE
                            , null);
                } else {
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_WIFI_DISABLE
                            , null);
                }

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that the state of Wi-Fi p2p connectivity
                // has changed.
                Log.e(TAG, "P2P CONNECTION CHANGED");
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_NETWORK_INFO);

                if(networkInfo.isConnected()) {
                    // We are connected with the other device, request connection
                    // info to find group owner IP
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_IS_CONNECTED
                            , networkInfo.getExtraInfo());
                    manager.requestConnectionInfo(channel, connectionInfoListener);
                }


            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that the available peer list has changed.
                Log.e(TAG, "P2P PEERS CHANGED");
                sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_PEERS_LIST, null);
                if (manager != null) {
                    manager.requestPeers(channel, peerListListener);
                }


            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that this device details have changed.


            } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that peer discovery has either started
                // or stopped.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);

                if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                    // started
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_DISCOVERY_STARTED
                            , null);
                } else {
                    // stopped
                    sendBroadcastToActivity(WiFiDirectConstants.BROADCAST_ACTION_DISCOVERY_STOPPED
                            , null);
                }

            }

        }
    }


    /** Add message listener.
     * @param listener */
    public void addListener(MessageListener listener) {
        mListener = listener;
    }


    /**
     * Send broadcast to fragment using Otto lib.
     * @param state String */
    private void sendBroadcastToActivity(final String state, String value) {
        Events.WifiState wifiState = new Events.WifiState();
        wifiState.state = state;
        wifiState.value = value;
        WifiDirectApplication.getBus().post(wifiState);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReciver);
    }



}
