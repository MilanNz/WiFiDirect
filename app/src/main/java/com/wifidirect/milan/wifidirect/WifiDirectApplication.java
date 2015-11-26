package com.wifidirect.milan.wifidirect;

import android.app.Application;

import com.squareup.otto.Bus;

/**
 * Created by milan on 26.11.15..
 */
public class WifiDirectApplication extends Application {
    private static Bus mBus = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public static Bus getBus(){
        if(mBus == null){
            mBus = new Bus();
        }
        return mBus;
    }



}
