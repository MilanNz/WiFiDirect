package com.wifidirect.milan.wifidirect.connections;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by milan on 27.11.15..
 */
public class WifiClient {
    private final String TAG = "WifiClient";
    private String address;
    private int port;
    private DataInputStream fromServer;
    private DataOutputStream toServer;

    public WifiClient setAddress(String address){
        this.address = address;
        return this;
    }

    public WifiClient setPort(int port){
        this.port = port;
        return this;
    }


    public void startClient() {
        try {
            Socket socket = new Socket(address, port);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {

        }
    }


    public void request() {
        try {
            Log.e(TAG, "Send message to server");
            toServer.writeBytes(TAG);
            toServer.flush();
            Log.e(TAG, "from server: " + fromServer.readByte());
        } catch (IOException e) {

        }

    }

}
