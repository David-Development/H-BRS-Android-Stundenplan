package de.luhmer.stundenplanh_brsimporter.app.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import de.luhmer.stundenplanh_brsimporter.app.Authentication.SisAccountAuthenticator;

/**
 * Created by David on 05.07.2014.
 */
public class SisAuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private SisAccountAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new SisAccountAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}