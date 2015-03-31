package edu.my.rstetsenko.zenua.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import edu.my.rstetsenko.zenua.Constants;

public class ZenUaSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ZenUaSyncAdapter sZenUaSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d(Constants.LOG_TAG, "onCreate - ZenUaSyncService");
        synchronized (sSyncAdapterLock) {
            if (sZenUaSyncAdapter == null) {
                sZenUaSyncAdapter = new ZenUaSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sZenUaSyncAdapter.getSyncAdapterBinder();
    }
}