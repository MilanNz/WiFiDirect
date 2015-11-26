package com.wifidirect.milan.wifidirect.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wifidirect.milan.wifidirect.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by milan on 26.11.15..
 */
public class DeviceAdapter extends ArrayAdapter<WifiP2pDevice> {
    private List<WifiP2pDevice> mDeviceList;

    public DeviceAdapter(Context context, List<WifiP2pDevice> deviceList) {
        super(context, 0);
        this.mDeviceList = deviceList;
    }

    public void refreshList(List<WifiP2pDevice> deviceList){
        this.mDeviceList.clear();
        this.mDeviceList.addAll(deviceList);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_device, parent, false);

        }

        TextView name = (TextView)convertView.findViewById(R.id.devicename);
        TextView address = (TextView)convertView.findViewById(R.id.devicetype);

        name.setText(String.valueOf(mDeviceList.get(position).deviceName));
        address.setText(String.valueOf(mDeviceList.get(position).deviceAddress));


        return convertView;
    }



    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getPosition(WifiP2pDevice item) {
        return super.getPosition(item);
    }

    @Override
    public int getCount() {
        return mDeviceList.size();
    }
}
