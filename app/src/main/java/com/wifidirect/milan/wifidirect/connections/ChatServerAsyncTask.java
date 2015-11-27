package com.wifidirect.milan.wifidirect.connections;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by milan on 27.11.15..
 */
public class ChatServerAsyncTask extends AsyncTask {
    private Context mContext;
    private static String TAG = "ChatServerTask";


    public ChatServerAsyncTask(Context context){
        this.mContext = context;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            // Create a server socket and wait for client connections. This call blocks until a
            // connection is accepted from a client
            ServerSocket serverSocket = new ServerSocket(8888);
            serverSocket.setReuseAddress(true);
            Socket socket = serverSocket.accept();

            // client is connected and transferred data.
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Object object = objectInputStream.readObject();

            if(object.getClass().equals(String.class) && ((String)object).equals("DIRECT")) {
                Log.e(TAG, "Client address: " + socket.getInetAddress());
            }
        } catch (IOException e) {

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
