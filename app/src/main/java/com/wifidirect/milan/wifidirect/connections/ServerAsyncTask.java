package com.wifidirect.milan.wifidirect.connections;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by milan on 26.11.15..
 */
public class ServerAsyncTask extends AsyncTask {
    private Context mContext;


    public ServerAsyncTask(Context context) {
        this.mContext = context;
    }


    @Override
    protected Object doInBackground(Object[] params) {
        try{
            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket client = serverSocket.accept();



        }catch (IOException e){}

        return null;
    }

}
