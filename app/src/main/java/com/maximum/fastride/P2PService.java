package com.maximum.fastride;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class P2PService extends Service {
    public P2PService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
