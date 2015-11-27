package com.wifidirect.milan.wifidirect.connections;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by milan on 27.11.15..
 */
public class ChatClientAsyncTask extends AsyncTask {
    private Context mContext;
    private String host;
    private byte buf[] = new byte[1024];


    public ChatClientAsyncTask(Context context, String host) {
        this.mContext = context;
        this.host = host;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            // create client socket with the host, port and timeout information.

            Log.e("ChatClientAsy", "doinBackground");
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.connect((new InetSocketAddress(host, 8888)), 500);

            Log.e("ChatClientAsy", "doinBackground");
            // create a byte stream
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeBytes(new String("DIRECT"));
            Log.e("ChatClientAsy", "doinBackground");
            objectOutputStream.close();
            outputStream.close();
            socket.close();

        } catch (IOException e) {

        }
        return null;
    }

}
