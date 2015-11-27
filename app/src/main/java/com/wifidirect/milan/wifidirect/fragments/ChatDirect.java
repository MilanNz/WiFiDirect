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

import com.wifidirect.milan.wifidirect.R;
import com.wifidirect.milan.wifidirect.activities.MainActivity;
import com.wifidirect.milan.wifidirect.adapters.ChatAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by milan on 26.11.15..
 */
public class ChatDirect extends Fragment {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, null);

        String name = getArguments().getString("name");
        String address = getArguments().getString("address");
        // action bar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set name
        if(name != null) {
            //((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(name);
            ((MainActivity) getActivity()).mToolbar.setSubtitle(name);
        }

        // bind butter knife lib
        ButterKnife.bind(this, view);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mAdapter = new ChatAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }



    @OnClick(R.id.imagebuttonsend)
    public void sendMessage(){
        String message = mEditText.getText().toString();
        mEditText.setText("");
        mAdapter.addMessage(message);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }




}
