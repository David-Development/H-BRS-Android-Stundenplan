package de.luhmer.stundenplanh_brsimporter.app.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import de.luhmer.stundenplanh_brsimporter.app.Adapter.ExamSyncAdapter;

/**
 * Created by David on 05.07.2014.
 */
public class ExamSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ExamSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new ExamSyncAdapter(this, true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}