package de.luhmer.stundenplanh_brsimporter.app.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.luhmer.stundenplanh_brsimporter.app.Helper.Constants;

public class BootCompletedReceiver extends BroadcastReceiver implements Constants {
    public BootCompletedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(mPrefs.getBoolean(ENABLE_REMINDER, false)) {
            //TimetableActivity.SetReminderUpdateTimer(context);
        }
    }
}
