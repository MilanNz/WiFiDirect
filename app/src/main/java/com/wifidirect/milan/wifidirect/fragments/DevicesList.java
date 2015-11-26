package com.wifidirect.milan.wifidirect.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by milan on 25.11.15..
 */
public class DevicesList extends Fragment{
    private static final String TAG = "DeviceList";
    @Bind(R.id.listview) ListView mListView;
    @Bind(R.id.emptyrelative) RelativeLayout mRelativeLayoutEmpty;
    @Bind(R.id.fab) FloatingActionButton mFloatingActionButton;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deviceslist, null);
        // action bar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

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

        return view;
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
        }
    }



    @Subscribe
    public void answerAvailable(Events.WifiState event) {
        // TODO: React to the event somehow!
        Toast.makeText(getActivity(), event.state, Toast.LENGTH_SHORT).show();

        if(MainActivity.mService == null) {
            return;
        }

        switch (event.state){
            case WiFiDirectConstants.BROADCAST_ACTION_PEERS_LIST:
                mDevicesList = MainActivity.mService.devicesList;

                if(mDevicesList.size() > 0){
                    mRelativeLayoutEmpty.setVisibility(View.GONE);
                } else {
                    mRelativeLayoutEmpty.setVisibility(View.VISIBLE);
                }

                // refresh devices list
                mAdapter.refreshList(mDevicesList);
                mAdapter.notifyDataSetChanged();


                break;

            case WiFiDirectConstants.BROADCAST_ACTION_WIFI_ENABLE:
                isWiFiEnable = true;
                break;

            case WiFiDirectConstants.BROADCAST_ACTION_WIFI_DISABLE:
                isWiFiEnable = false;
                dialog();

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
                WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
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
        builder.setItems(new String[]{"More Informations" ,"Send message", "Send file"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    Log.e(TAG, "list position " + String.valueOf(position));
                    Log.e(TAG, "list size " + String.valueOf(mDevicesList.size()));
                    try {
                        dialogInfo(mDevicesList.get(position));
                    }catch (ArrayIndexOutOfBoundsException e){
                        Log.e(TAG, e.getMessage());
                    }
                } else if(which == 1) {

                } else if(which == 2) {

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

        ((TextView)dialogv.findViewById(R.id.deviceName)).setText(String.valueOf(device.deviceName));
        ((TextView)dialogv.findViewById(R.id.devicemacaddress)).setText(String.valueOf(device.deviceAddress));
        ((TextView)dialogv.findViewById(R.id.deviceprimarydevicetype)).setText(String.valueOf(device.primaryDeviceType));
        ((TextView)dialogv.findViewById(R.id.devicesecondarydevicetype)).setText(String.valueOf(device.secondaryDeviceType));
        ((TextView)dialogv.findViewById(R.id.devicestatuse)).setText(String.valueOf(device.status));

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        WifiDirectApplication.getBus().unregister(this);
    }


}
