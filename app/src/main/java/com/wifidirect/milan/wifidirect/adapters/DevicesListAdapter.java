package com.wifidirect.milan.wifidirect.adapters;

import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wifidirect.milan.wifidirect.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by milan on 25.11.15..
 */
public class DevicesListAdapter extends RecyclerView.Adapter<DevicesListAdapter.ViewHolder> {
    private List<WifiP2pDevice> devicesList = new ArrayList<>();
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextViewName;
        public TextView mTextViewType;
        public TextView mTextViewPrimary;
        public TextView mTextViewStatus;
        public ImageView mImageViewIcon;

        public ViewHolder(View v) {
            super(v);
            mTextViewName = (TextView)v.findViewById(R.id.devicename);
            mTextViewType = (TextView)v.findViewById(R.id.devicetype);
            mTextViewPrimary = (TextView)v.findViewById(R.id.primaryDevice);
            mTextViewStatus = (TextView)v.findViewById(R.id.devicestatus);
            mImageViewIcon = (ImageView)v.findViewById(R.id.deviceicon);
        }
    }

    public DevicesListAdapter(List<WifiP2pDevice> devicesList) {
        this.devicesList = devicesList;
    }


    public void addItem(WifiP2pDevice wifiP2pDevice){
        devicesList.add(wifiP2pDevice);
        notifyDataSetChanged();
    }

    public void refreshDevicesList(List<WifiP2pDevice> devicesList){
        this.devicesList = devicesList;
        notifyDataSetChanged();
    }

    @Override
    public DevicesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_device, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DevicesListAdapter.ViewHolder holder, int position) {
        holder.mTextViewName.setText(devicesList.get(position).deviceName);
        holder.mTextViewPrimary.setText(devicesList.get(position).primaryDeviceType);
        holder.mTextViewType.setText(devicesList.get(position).deviceAddress);
        holder.mTextViewStatus.setText(String.valueOf(devicesList.get(position).status));

    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }


}
