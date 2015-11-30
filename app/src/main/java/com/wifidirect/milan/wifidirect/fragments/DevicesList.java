package com.wifidirect.milan.wifidirect.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.wifidirect.milan.wifidirect.Events;
import com.wifidirect.milan.wifidirect.R;
import com.wifidirect.milan.wifidirect.WiFiDirectConstants;
import com.wifidirect.milan.wifidirect.WifiDirectApplication;
import com.wifidirect.milan.wifidirect.activities.MainActivity;
import com.wifidirect.milan.wifidirect.adapters.DeviceAdapter;
import com.wifidirect.milan.wifidirect.listeners.MessageListener;
import com.wifidirect.milan.wifidirect.notifications.WifiNotification;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by milan on 25.11.15..
 */
public class DevicesList extends Fragment implements MessageListener{
    private static final String TAG = "DeviceList";
    @Bind(R.id.listview) ListView mListView;
    @Bind(R.id.emptyrelative) RelativeLayout mRelativeLayoutEmpty;
    @Bind(R.id.fab) FloatingActionButton mFloatingActionButton;
    @Bind(R.id.progressbar) ProgressBar mProgressbar;
    private boolean isWiFiEnable;
    private DeviceAdapter mAdapter;
    private List<WifiP2pDevice> mDevicesList = new ArrayList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstanceState) {
        // inflate fragment
        View view = inflater.inflate(R.layout.fragment_deviceslist, null);
        // action bar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");

        // ButterKnife lib
        ButterKnife.bind(this, view);

        // register otto
        WifiDirectApplication.getBus().register(this);

        // listview array adapter
        mAdapter = new DeviceAdapter(getActivity(), mDevicesList);
        mListView.setAdapter(mAdapter);

        // set flaoting action button on click listener
        mFloatingActionButton.setOnClickListener(new FloatOnClickListener());

        // set listview item click listener
        mListView.setOnItemClickListener(new ListViewOnClickItem());

        // add listener
        if(MainActivity.mService != null) {
            MainActivity.mService.addListener(this);
        }

        return view;
    }



    @Override
    public void onMessageReceived(String response) {
        // create notification
        WifiNotification.createNotification(getActivity(), response);
    }

    @Override
    public void onConnected(boolean isConnected) {
        Toast.makeText(getActivity(), "CONNECTED!", Toast.LENGTH_SHORT).show();
    }


    /** ListView on click listener. */
    private class ListViewOnClickItem implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            dialogOptions(position);
        }
    }


    /** Floating button on click listener. */
    private class FloatOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Snackbar.make(v, "Searching", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            if(MainActivity.mService != null) {
                MainActivity.mService.refreshList();
            }
        }
    }



    @Subscribe
    public void answerAvailable(Events.WifiState event) {
        if(MainActivity.mService == null) {
            return;
        }

        switch (event.state){
            // new list of peers
            case WiFiDirectConstants.BROADCAST_ACTION_PEERS_LIST:
                // clear list of devices and add new list
                mDevicesList.clear();
                mDevicesList.addAll(MainActivity.mService.mDevicesList);

                // show or hide RelativeLayout
                if(mDevicesList.size() > 0) {
                    mRelativeLayoutEmpty.setVisibility(View.GONE);
                } else {
                    mRelativeLayoutEmpty.setVisibility(View.VISIBLE);
                }

                // refresh adapter
                mAdapter.clear();
                mAdapter.addAll(mDevicesList);
                mAdapter.notifyDataSetChanged();

                Snackbar.make(getView(), "New devices", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                break;

            // Wifi enable
            case WiFiDirectConstants.BROADCAST_ACTION_WIFI_ENABLE:
                isWiFiEnable = true;
                break;

            // Wifi disable
            case WiFiDirectConstants.BROADCAST_ACTION_WIFI_DISABLE:
                isWiFiEnable = false;
                dialog();
                break;

            case WiFiDirectConstants.BROADCAST_ACTION_IS_CONNECTED:
                Toast.makeText(getActivity(), "CONNECTED", Toast.LENGTH_SHORT);
                break;

            case WiFiDirectConstants.BROADCAST_ACTION_INFO_GROUP_FORMED_CLIENT:
                Snackbar.make(getView(), "CONNECTED, you are Client!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;

            case WiFiDirectConstants.BROADCAST_ACTION_INFO_GROUP_FORMED_OWNER:
                Snackbar.make(getView(), "CONNECTED, you are OWNER!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;

            case WiFiDirectConstants.BROADCAST_ACTION_DISCOVERY_STARTED:
                mProgressbar.setVisibility(View.VISIBLE);
                break;

            case WiFiDirectConstants.BROADCAST_ACTION_DISCOVERY_STOPPED:
                mProgressbar.setVisibility(View.GONE);
                break;

            default:
                break;
        }


    }


    /** Alert dialog if wifi is disabled */
    private void dialog()  {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Your WiFi is off");
        builder.setMessage("Please enable WiFi");
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // enable wifi
                WifiManager wifiManager = (WifiManager) getActivity()
                        .getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create();
        builder.show();
    }


    /** Alert dialog for optiosn.
     * @param position int */
    private void dialogOptions(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Options");
        builder.setItems(new String[]{"More Informations", "Connect", "Send message"}
                , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // info
                if (which == 0) {
                    try {
                        dialogInfo(mDevicesList.get(position));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    // connect
                } else if (which == 1) {

                    MainActivity.mService.connectToDevice(mDevicesList.get(position));

                    // send message
                } else if (which == 2) {
                    replaceFragment(new ChatDirect(), mDevicesList.get(position).deviceName
                            , mDevicesList.get(position).deviceAddress);
                }

            }
        });
        builder.create();
        builder.show();
    }


    /** Alert dialog for info.
     * @param device WifiP2pDevice */
    private void dialogInfo(final WifiP2pDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogv = LayoutInflater.from(getActivity()).inflate(R.layout.dialog, null);

        ((TextView)dialogv.findViewById(R.id.deviceName))
                .setText(String.valueOf(device.deviceName));
        ((TextView)dialogv.findViewById(R.id.devicemacaddress))
                .setText(String.valueOf(device.deviceAddress));
        ((TextView)dialogv.findViewById(R.id.deviceprimarydevicetype))
                .setText(String.valueOf(device.primaryDeviceType));
        ((TextView)dialogv.findViewById(R.id.devicesecondarydevicetype))
                .setText(String.valueOf(device.secondaryDeviceType));
        ((TextView)dialogv.findViewById(R.id.devicestatuse))
                .setText(String.valueOf(device.status));

        builder.setView(dialogv);
        builder.setTitle("Info");
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create();
        builder.show();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    /** Replace fragment with existing. */
    private void replaceFragment(Fragment fragment, String name, String address){
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("address", address);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.container, fragment).addToBackStack("chat.fragment");
        fragmentTransaction.commit();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.refresh) {
            if(MainActivity.mService != null) {
                MainActivity.mService.refreshList();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        WifiDirectApplication.getBus().unregister(this);
    }


}
