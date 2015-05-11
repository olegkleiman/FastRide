package com.maximum.fastride.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Oleg on 09-May-15.
 */
public class ChatManager implements Runnable {

    private static final String LOG_TAG = "FR.Chat";

    private Socket socket = null;
    private Handler handler;

    private InputStream iStream;
    private OutputStream oStream;

    public ChatManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;

        Message msg = handler.obtainMessage(Globals.TRACE_MESSAGE);
        Bundle bundle = new Bundle();
        String strMessage = String.format("Local socket address: %s, Remote address: %s",
                        socket.getLocalSocketAddress(),
                        socket.getRemoteSocketAddress());
        Log.d(LOG_TAG, strMessage);
        bundle.putString("message", strMessage);
        msg.setData(bundle);
        msg.sendToTarget();
    }

    @Override
    public void run() {
        try{

            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                // Read from the InputStream
                bytes = iStream.read(buffer);
                if (bytes == -1) {
                    break;
                }

                handler.obtainMessage(Globals.MESSAGE_READ,
                        bytes, -1, buffer).sendToTarget();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    }
}
