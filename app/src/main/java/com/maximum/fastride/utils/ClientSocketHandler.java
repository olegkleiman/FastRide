package com.maximum.fastride.utils;

import android.os.Handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Oleg Kleiman on 09-May-15.
 */
public class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private ITrace mTracer;
    private String mMessage;

    private ChatManager chat;
    private InetAddress mAddress;

    public ClientSocketHandler(Handler handler,
                               InetAddress groupOwnerAddress,
                               ITrace tracer,
                               String message) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
        mTracer = tracer;
        this.mMessage = message;
    }

    @Override
    public void run() {

        try {

            Socket socket = new Socket();

            socket.bind(null);
            InetAddress localAddress = socket.getLocalAddress();

            String traceMessage = String.format("Local socket. Address: %s. Port: %d",
                    localAddress.getHostAddress(),
                    socket.getLocalPort());
            mTracer.trace(traceMessage);

            socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
                                                Globals.SERVER_PORT), 5000);

            OutputStream os = socket.getOutputStream();
            os.write(mMessage.getBytes());

            chat = new ChatManager(socket, handler);
            new Thread(chat).start();

            socket.close();

        } catch (IOException e) {

            mTracer.trace(e.getMessage());

            return;
        }
    }
}
