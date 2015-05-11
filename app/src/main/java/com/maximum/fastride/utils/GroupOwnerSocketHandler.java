package com.maximum.fastride.utils;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Oleg Kleiman on 09-May-15.
 */
public class GroupOwnerSocketHandler extends Thread{

    ServerSocket socket = null;
    private final int THREAD_COUNT = 10;
    private Handler handler;

    private static final String TAG = "GroupOwnerSocketHandler";

    public GroupOwnerSocketHandler(Handler handler) throws IOException {
        socket = new ServerSocket(Globals.SERVER_PORT);
        this.handler = handler;
    }

    /**
     * A ThreadPool for client sockets.
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void run() {
        while( true ) {
            try{
                pool.execute(new ChatManager(socket.accept(), handler));
            }
            catch(IOException e) {

                try {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                } catch (IOException ioe) {

                }

                Log.e(TAG, e.getMessage());
                pool.shutdownNow();
                break;
            }
        }
    }
}
