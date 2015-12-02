package com.wifidirect.milan.wifidirect.connections;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.wifidirect.milan.wifidirect.utils.MessageUtils;
import com.wifidirect.milan.wifidirect.listeners.SocketListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private ClientConnectTask mClientConnectionTask;
    private ServerConnectTask mServerConnectionTask;

    private MessageTask mMessageTask;
    private FileTask mFileTask;

    private byte buff[] = new byte[1024];
    private Context mContext;
    private File path = Environment.getExternalStorageDirectory().getAbsoluteFile();



    public SocketTransfer(Context context) {
        this.mContext = context;
    }

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
        // mServerSideTask = new ServerSideTask();
        // mServerSideTask.execute();
        mServerConnectionTask = new ServerConnectTask();
        mServerConnectionTask.execute();

    }


    @Override
    public void startClient() {
        // start client
        // mClientSideTask = new ClientSideTask();
        // mClientSideTask.execute();
        mClientConnectionTask = new ClientConnectTask();
        mClientConnectionTask.execute();
    }

    /** Client connection Task. */
    private class ClientConnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                // connect to server
                mSocket = new Socket();
                mSocket.bind(null);
                mSocket.connect(new InetSocketAddress(mAddress, mPort), 500);
                Log.e(TAG, "Connect on port: " + mPort + ", address: " + mAddressIno);

                // is connected?
                if (mSocket.isConnected()) {
                    Log.e(TAG, "Client connected: " + mSocket.getRemoteSocketAddress());
                    return true;
                }
            } catch(IOException e) {
                System.out.println(TAG + e.getMessage());
            }
            return false;
        }
    }


    /** Server connect task. */
    private class ServerConnectTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                // create server
                mServerSocket = new ServerSocket(mPort);
                Log.e(TAG, "Waiting for client on port: " + mServerSocket.getLocalPort());

                // create client
                mSocket = mServerSocket.accept();

                if (mSocket.isConnected()) {
                    Log.e(TAG, "Client connected!");
                    return true;
                }

            } catch(IOException e) {
                System.out.println(TAG + e.getMessage());
            }
            return false;
        }

    }


    private class MessageTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try{
                // create client for dataout and datain
                mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                mDataInputStream = new DataInputStream(mSocket.getInputStream());

                // reciver
                while (mSocket.isConnected()) {
                    Log.e(TAG, "Receive!");
                    if (mListener != null) {
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


    /** Client File side. */
    private class FileTask extends AsyncTask<Void, Void, Void> {
        private Context context;

        public FileTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                int len;
                // is it client connected
                if(mSocket == null) {
                    return null;
                } else {
                    Log.e(TAG, "Socket null");
                }

                if(mSocket.isConnected()) {
                    return null;
                } else {
                    Log.e(TAG, "mSocket isn't connected!");
                }

                Log.e(TAG, "FileTaskOutputstream");

                OutputStream outputStream = mSocket.getOutputStream();
                ContentResolver contentResolver = context.getContentResolver();
                InputStream inputStream = null;
                inputStream = contentResolver.openInputStream(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/milan.jpg"));

                while ((len = inputStream.read(buff)) != -1) {
                    outputStream.write(buff, 0, len);
                    Log.e(TAG, "Write!");
                }

                outputStream.close();
                inputStream.close();

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }
    }




    @Override
    public void closeConnection() {
        try {
            mSocket.close();
            mServerSocket.close();
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


    @Override
    public void sendFile(String path) {
        SendFileTask sendFileTask = new SendFileTask(path);
        sendFileTask.execute();
    }


    private class SendFileTask extends AsyncTask<Void, Void, Void> {
        private String path;

        public SendFileTask(String path) {
            this.path = path;
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.e(TAG, "Send file");
                sendMessage(MessageUtils.createMessage("milan", "not important", MessageUtils.TYPE_FILE
                        , 22, MessageUtils.TYPE_IMAGE));

                File file = new File(path);
                if (!file.exists() || file.isDirectory()) {
                    return null;
                }
                mDataInputStream = new DataInputStream(mSocket.getInputStream());
                copyFiles(mDataInputStream, new FileOutputStream(file));

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return null;
        }

        /** Copy files.*/
        private void copyFiles(DataInputStream dataInputStream, FileOutputStream fileOutputStream) {
            try{
                int read = 0;
                while((read = dataInputStream.read(buff)) != -1){
                    fileOutputStream.write(buff, 0, read);
                    Log.e(TAG, "write image!");
                }
            }catch (IOException e){}
        }
    }




    /*
    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }*/




    @Override
    public void startMessageReceiver() {
        mMessageTask = new MessageTask();
        mMessageTask.execute();
    }

    @Override
    public void startFileReceiver() {
        Log.e(TAG, "start file receiver");
        mFileTask = new FileTask(mContext);
        mFileTask.execute();
    }

    @Override
    public void stopMessageReceiver() {
        if(mMessageTask != null) {
            mMessageTask.cancel(true);
        }
    }

    @Override
    public void stopFileReceiver() {
        if(mFileTask != null) {
            mFileTask.cancel(true);
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
