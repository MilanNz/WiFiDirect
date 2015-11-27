package com.wifidirect.milan.wifidirect.connections;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by milan on 27.11.15..
 */
public class WifiServer {
    private static String TAG = "WifiServer";
    private int port;
    private boolean isServerRunning = true;



    public WifiServer setPort(int port){
        this.port = port;
        return this;
    }

    public void stopServer(){
        this.isServerRunning = false;
    }


    public void startServer(){
        DataInputStream fromClient;
        DataOutputStream toClient;
        ServerSocket serverSocket;
        Socket socket;
        String word = "Hello Device!";
        try {
            Log.e(TAG, "Server started...");
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            serverSocket.close();

            fromClient = new DataInputStream(socket.getInputStream());
            toClient = new DataOutputStream(socket.getOutputStream());

            while(isServerRunning) {
                word = fromClient.readLine();
                Log.e(TAG, word);

                toClient.writeBytes(word);
                toClient.flush();

                Log.e(TAG, "done");
                this.startServer();
            }
        } catch (IOException e) {}
    }


}
