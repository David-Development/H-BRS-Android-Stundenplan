package de.luhmer.stundenplanh_brsimporter.app.Receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.luhmer.stundenplanh_brsimporter.app.Helper.Constants;
import de.luhmer.stundenplanh_brsimporter.app.Model.TimetableEntry;
import de.luhmer.stundenplanh_brsimporter.app.TimetableImporterActivity;

public class UpdateReceiver extends BroadcastReceiver implements Constants {
    public UpdateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // create a period starting now with a duration of one (1) day..
        Period period = new Period(new DateTime(new Date().getTime()), new Dur(1, 0, 0, 0));
        //Period period = new Period(new DateTime(new Date().getTime() + (AlarmManager.INTERVAL_DAY * 2) ), new Dur(1, 0, 0, 0));
        Filter filter = new Filter(new PeriodRule(period));

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String cal_Data = mPrefs.getString(TimetableImporterActivity.calendarString, "");

        List<TimetableEntry> entries = TimetableImporterActivity.getTimetableEntrysByCalendar(context, cal_Data, filter);
        Collections.sort(entries);

        String[] stringsToRemove = new String[] { "(Übung)", "(Vorlesung)" };

        for(TimetableEntry entry : entries) {
            for(String remove : stringsToRemove)
                entry.summary = entry.summary.replace(remove, "").trim();

            entry.location = entry.location.replace("Hochschule Bonn-Rhein-Sieg, Raum:", "").trim();

            SetReminder(context, entry);
        }
    }


    private void SetReminder(Context context, TimetableEntry timetableEntry) {
        long timeInMillis = timetableEntry.dtstart;

        Intent intent = new Intent(context, UpdateReceiver.class);
        intent.putExtra(NotificationReceiver.TEXT_CONTENT, timetableEntry.description);
        intent.putExtra(NotificationReceiver.TEXT_TITLE, timetableEntry.summary);

        intent.setAction("de.luhmer.stundenplanh_brsimporter.SHOW_NOTIFICATION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

}
