package com.wifidirect.milan.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wifidirect.milan.wifidirect.adapters.DevicesListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by milan on 25.11.15..
 */
public class DevicesList extends Fragment implements WifiP2pManager.ChannelListener, WifiP2pManager.ActionListener {
    private static final String TAG = "DeviceList";
    @Bind(R.id.recycler_view) RecyclerView mRecyclerViewDevices;
    @Bind(R.id.emptyrelative) RelativeLayout mRelativeLayoutEmpty;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private DevicesListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Wifi2P2 reciver;
    private WifiP2pManager manager;
    WifiP2pManager.PeerListListener peerListListener;
    private List<WifiP2pDevice> devicesList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deviceslist, null);

        // ButterKnife lib
        ButterKnife.bind(this, view);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerViewDevices.setLayoutManager(layoutManager);

        // specify an adapter
        adapter = new DevicesListAdapter(devicesList);
        mRecyclerViewDevices.setAdapter(adapter);


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


        // wifi manager
        manager = (WifiP2pManager)getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(getActivity(), Looper.getMainLooper(), null);
        reciver = new Wifi2P2(manager, channel, getActivity());


        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Searching", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                /*
                if(manager != null) {
                    manager.requestPeers(channel, peerListListener);
                }*/

            }
        });


        // descovering peers
        manager.discoverPeers(channel, this);

        // register peer list listener
        peerListListener = new WifiP2pManager.PeerListListener(){
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                // clean devices list
                if(!devicesList.isEmpty()) {
                    devicesList.clear();
                }

                for(WifiP2pDevice device : peers.getDeviceList()){
                    devicesList.add(device);
                }

                if(devicesList.size() != 0){
                    mRelativeLayoutEmpty.setVisibility(View.GONE);
                } else {
                    mRelativeLayoutEmpty.setVisibility(View.VISIBLE);
                }
                // refresh devices list
                adapter.refreshDevicesList(devicesList);
            }
        };

        return view;
    }

    @Override
    public void onChannelDisconnected() {

    }

    @Override
    public void onSuccess() {
        if(manager != null) {
            manager.requestPeers(channel, peerListListener);
        }
    }

    @Override
    public void onFailure(int reason) {
        Toast.makeText(getActivity(), "failure", Toast.LENGTH_LONG).show();
    }


    private class Wifi2P2 extends BroadcastReceiver{
        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private Context mActivity;


        public Wifi2P2(WifiP2pManager manager, WifiP2pManager.Channel channel, Context activity) {
            super();
            this.mManager = manager;
            this.mChannel = channel;
            this.mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled.
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // Wifi p2p is enabled
                    Toast.makeText(getActivity(), "WifiP2p enable", Toast.LENGTH_LONG).show();
                } else {
                    // Wifi p2p is disabled
                    Toast.makeText(getActivity(), "WifiP2p disable", Toast.LENGTH_LONG).show();
                }

            } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that the available peer list has changed.
                // The peer list has changed!  We should probably do something about
                // that.
                Toast.makeText(getActivity(), "List changed", Toast.LENGTH_LONG).show();
                if(manager != null){
                    manager.requestPeers(channel, peerListListener);
                }

            } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that the state of Wi-Fi p2p connectivity has changed.
                // Connection state changed!  We should probably do something about
                // thate.
                Toast.makeText(getActivity(), "Connection has changed", Toast.LENGTH_SHORT).show();

            } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that this device details have changed.

            } else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that peer discovery has either started or stopped.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
                if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
                    Toast.makeText(getActivity(), "Discovery started", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Discovery stopted", Toast.LENGTH_SHORT).show();
                }


            }

        }


    }




    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(reciver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(reciver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
