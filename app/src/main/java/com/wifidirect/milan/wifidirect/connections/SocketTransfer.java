package com.wifidirect.milan.wifidirect.connections;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.wifidirect.milan.wifidirect.listeners.SocketListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by milan on 30.11.15..
 * https://github.com/MilanNz/Java-Socket-Client-Server
 */
public class SocketTransfer implements Transfer {
    private static final String TAG = "Transfer ";
    private int mPort;
    private String mAddress;
    private InetAddress mAddressIno;
    private Socket mSocket;
    private ServerSocket mServerSocket;
    private DataOutputStream mDataOutputStream;
    private DataInputStream mDataInputStream;
    private SocketListener mListener;
    private ServerSideTask mServerSideTask;
    private ClientSideTask mClientSideTask;



    @Override
    public Transfer setPort(int port) {
        this.mPort = port;
        return this;
    }


    @Override
    public Transfer setAddress(String address) {
        this.mAddress = address;
        return this;
    }


    @Override
    public Transfer setInetAddress(InetAddress address) {
        this.mAddressIno = address;
        return this;
    }


    @Override
    public void startServer() {
        // create server
        mServerSideTask = new ServerSideTask();
        mServerSideTask.execute();

    }


    @Override
    public void startClient() {
        // start client
        mClientSideTask = new ClientSideTask();
        mClientSideTask.execute();
    }


    /** Server side task. */
    private class ServerSideTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {
                // create server
                mServerSocket = new ServerSocket(mPort);
                Log.e(TAG, "Waiting for client on port: " + mServerSocket.getLocalPort());

                // create client
                mSocket = mServerSocket.accept();

                if(mSocket.isConnected()) {
                    Log.e(TAG, "Client connected!");
                }

                // create client for out and in
                mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                mDataInputStream = new DataInputStream(mSocket.getInputStream());

                // sendMessage("receive");
                // reciver
                while(mSocket.isConnected()){
                    Log.e(TAG, "Receive!");
                    if(mListener != null) {
                        mListener.onReceiver(base64decode(mDataInputStream.readUTF()));
                    }
                }

                // close data output and input stream
                mDataOutputStream.close();
                mDataInputStream.close();
                // close serversocket
                mServerSocket.close();

            } catch(IOException e) {
                System.out.println(TAG + e.getMessage());
            }
            return null;
        }
    }


    /** Client side task. */
    private class ClientSideTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {
                // connect to server
                mSocket = new Socket();
                mSocket.bind(null);
                mSocket.connect(new InetSocketAddress(mAddress, mPort), 500);
                Log.e(TAG, "Connect on port: " + mPort + ", address: " + mAddressIno);

                // is connected?
                if(mSocket.isConnected()) {
                    Log.e(TAG, "Client connected: " + mSocket.getRemoteSocketAddress());
                }

                // create client for dataout and datain
                mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                mDataInputStream = new DataInputStream(mSocket.getInputStream());

                // sendMessage("receive");
                // reciver
                while(mSocket.isConnected()){
                    Log.e(TAG, "Receive!");
                    if(mListener != null) {
                        mListener.onReceiver(base64decode(mDataInputStream.readUTF()));

                    }
                }

                // close data output and input stream
                mDataInputStream.close();
                mDataOutputStream.close();

                // close socket
                mSocket.close();
            } catch(IOException e) {
                System.out.println(TAG + e.getMessage());
            }

            return null;
        }
    }




    @Override
    public void closeConnection() {
        try {
            mSocket.close();
            mServerSocket.close();

            if(mServerSideTask != null) {
                mServerSideTask.cancel(true);
            }
            if(mClientSideTask != null) {
                mClientSideTask.cancel(true);
            }
        } catch (IOException ex) {
            Logger.getLogger(SocketTransfer.class.getName()).log(Level.SEVERE
                    , null, ex);
        }
    }


    @Override
    public void sendMessage(String message) {
        try {
            mDataOutputStream.writeUTF(base64encode(message));
            mDataOutputStream.flush();
        } catch (IOException | NullPointerException ex) {
            System.out.println(TAG + ex.getMessage());
        }
    }


    /** Encode message with Base64.
     @param message */
    private String base64encode(String message){
        try {
            return Base64.encodeToString(message.getBytes(), Base64.DEFAULT);
        } catch (Exception ex) {
            Logger.getLogger(SocketTransfer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    /** Decode message using Base64.
     @param message */
    private String base64decode(String message){
        try {
            byte[] base64decodedBytes = Base64.decode(message, Base64.DEFAULT);
            return new String(base64decodedBytes, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SocketTransfer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    @Override
    public void addListener(SocketListener listener) {
        this.mListener = listener;
    }

}
