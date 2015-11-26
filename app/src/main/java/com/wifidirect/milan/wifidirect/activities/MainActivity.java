package com.wifidirect.milan.wifidirect.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.wifidirect.milan.wifidirect.R;
import com.wifidirect.milan.wifidirect.fragments.DevicesList;
import com.wifidirect.milan.wifidirect.services.WifiDirectService;

public class MainActivity extends AppCompatActivity implements ServiceConnection{
    public static WifiDirectService mService;
    public static boolean isBind;
    public Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if(savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.container, new DevicesList()).commit();
        }

        // start
        startAndBindService();

    }


    /** Start and bind service. */
    public void startAndBindService() {
        Intent intent = new Intent(this, WifiDirectService.class);
        startService(intent);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    /** Stop and unbind service. */
    public void stopAndUnbindService() {
        Intent intent = new Intent(this, WifiDirectService.class);
        stopService(intent);
        unbindService(this);
    }


    // connect - dissconnect from service
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = ((WifiDirectService.ServiceBinder) service).getService();
        isBind = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isBind = false;
    }



    // main menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stop service
        stopAndUnbindService();
    }

}
