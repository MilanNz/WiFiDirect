package com.wifidirect.milan.wifidirect.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.wifidirect.milan.wifidirect.Events;
import com.wifidirect.milan.wifidirect.listeners.MessageListener;
import com.wifidirect.milan.wifidirect.R;
import com.wifidirect.milan.wifidirect.WiFiDirectConstants;
import com.wifidirect.milan.wifidirect.WifiDirectApplication;
import com.wifidirect.milan.wifidirect.activities.MainActivity;
import com.wifidirect.milan.wifidirect.adapters.ChatAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by milan on 26.11.15..
 */
public class ChatDirect extends Fragment implements MessageListener {
    private static final String TAG = "ChatDirect ";
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.edittextmessage) EditText mEditText;
    private ChatAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, null);

        // get name
        String name = getArguments().getString("name");

        // action bar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set subtitle on actionbar
        if (name != null) {
            ((MainActivity) getActivity()).mToolbar.setSubtitle(name);
        }

        // bind butter knife lib
        ButterKnife.bind(this, view);

        // register otto
        WifiDirectApplication.getBus().register(this);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mAdapter = new ChatAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        // add listener
        if (MainActivity.mService != null) {
            MainActivity.mService.addListener(this);
        }

        // remove all notifications
        // WifiNotification.removeNotification();

        return view;
    }



    @OnClick(R.id.imagebuttonsend)
    public void sendMessage() {
        String message = mEditText.getText().toString();
        mEditText.setText("");
        mAdapter.addMessage(message, true);
        MainActivity.mService.sendMessage(message);
    }


    @Override
    public void onMessageReceived(final String response) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.addMessage(response, false);
                // mAdapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public void onConnected(boolean isConnected) {
        Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
    }


    @Subscribe
    public void answerAvailable(Events.WifiState event) {
        if (MainActivity.mService == null) {
            return;
        }

        switch (event.state) {
            // if WiFi is eneble
            case WiFiDirectConstants.BROADCAST_ACTION_WIFI_ENABLE:
                Toast.makeText(getActivity(), "Your WiFi is enabled", Toast.LENGTH_SHORT).show();
                break;

            // if WiFi is disable
            case WiFiDirectConstants.BROADCAST_ACTION_WIFI_DISABLE:
                Toast.makeText(getActivity(), "Your Wifi is disabled!", Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed();
                break;

            default:
                break;
        }


    }


    // Main Menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v
            , ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
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
