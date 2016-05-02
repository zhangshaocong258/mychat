package com.szl.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by zsc on 2016/5/2.
 */
public class Disconnect {
    public static void disconnect(ServerSocket serverSocket, Socket socket,
                                  DataInputStream dataInputStream, DataOutputStream dataOutputStream){
        try {
            if (serverSocket != null) serverSocket.close();
            if (socket != null) socket.close();
            if (dataInputStream != null) dataInputStream.close();
            if (dataOutputStream != null) dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
