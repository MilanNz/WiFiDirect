package com.wifidirect.milan.wifidirect.connections;

import com.wifidirect.milan.wifidirect.listeners.SocketListener;

import java.net.InetAddress;

/**
 * Created by milan on 30.11.15..
 * https://github.com/MilanNz/Java-Socket-Client-Server
 */
public interface Transfer {
    /** Set server port.
     @param port */
    Transfer setPort(int port);
    /** Set server address.
     @port address */
    Transfer setAddress(String address);
    /** Start server. */
    void startServer();
    /** Start client. */
    void startClient();
    /** Close connection. */
    void closeConnection();
    /** Send message.
     @param message */
    void sendMessage(String message);
    /** Add listener
     @param listener */
    void addListener(SocketListener listener);
    /** Set InetAddress
     * @param address InetAddress*/
    Transfer setInetAddress(InetAddress address);
    /** Send file.
     * @param path */
    void sendFile(String path);
    /** Start message receiver. */
    void startMessageReceiver();
    /** Start file receiver. */
    void startFileReceiver();
    /** Stop message receiver. */
    void stopMessageReceiver();
    /** Stop file receiver. */
    void stopFileReceiver();
}
