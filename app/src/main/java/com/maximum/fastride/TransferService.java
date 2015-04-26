package com.maximum.fastride;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.maximum.fastride.utils.Globals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TransferService extends IntentService {

    final String LOG_TAG = "FR.TransferService";

    public TransferService() {
        super("TransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            ServerSocket serverSocket = new ServerSocket(Globals.SERVER_PORT);

            String traceMessage = "Server: Socket opened on port " + Globals.SERVER_PORT;
            Log.d(LOG_TAG, traceMessage);

            Socket clientSocket = serverSocket.accept();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            traceMessage = reader.readLine();
            Log.d(LOG_TAG, traceMessage);

            serverSocket.close();

            traceMessage = "Server socket closed";
            Log.d(LOG_TAG, traceMessage);

        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }


    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
