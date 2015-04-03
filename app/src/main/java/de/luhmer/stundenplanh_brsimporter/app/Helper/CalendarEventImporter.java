package de.luhmer.stundenplanh_brsimporter.app.Helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Log;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

import de.luhmer.stundenplanh_brsimporter.app.Model.TimetableEntry;


/**
 * Created by david on 02.04.14.
 */
public class CalendarEventImporter {
    private static final String TAG = "CalendarEventImporter";
    final static String[] CALENDAR_QUERY_COLUMNS = {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.NAME,
            CalendarContract.Calendars.VISIBLE,
            CalendarContract.Calendars.OWNER_ACCOUNT
    };



    public static HashMap<String, String> getAvailableCalendars(Context context) {
        HashMap calendarMap = new HashMap<String, String>();

        ContentResolver contentResolver = context.getContentResolver();
        Log.d(TAG, "URI = " + CalendarContract.Calendars.CONTENT_URI);
        final Cursor cursor = contentResolver.query(CalendarContract.Calendars.CONTENT_URI,
                CALENDAR_QUERY_COLUMNS, null, null, null);
        Log.d(TAG, "cursor = " + cursor);
        while (cursor.moveToNext()) {
            final String _id = cursor.getString(0);
            final String displayName = cursor.getString(1);
            final Boolean selected = !cursor.getString(2).equals("0");
            final String accountName = cursor.getString(3);
            Log.d(TAG, "Found calendar " + accountName);

            if(displayName != null)
                calendarMap.put(_id, displayName);
            Log.d(TAG,
                    "Calendar: Id: " + _id + " Display Name: " + displayName + " Selected: " + selected + " Name " + accountName);
        }
        return calendarMap;
    }


    public static final String CAL_EVENT_IDS = "CAL_EVENT_IDS";
    public static  void InsertCalEvents(Context context, TimetableEntry entry, long calID) {

        /*
        if(new Date(entry.dtstart).getTimezoneOffset() == new Date().getTimezoneOffset()) {
            entry.dtstart -= AlarmManager.INTERVAL_HOUR;
            entry.dtend -= AlarmManager.INTERVAL_HOUR;
        }
        */



        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, entry.dtstart);
        values.put(CalendarContract.Events.DTEND, entry.dtend);
        values.put(CalendarContract.Events.TITLE, entry.summary);
        values.put(CalendarContract.Events.DESCRIPTION, entry.description);
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Berlin");
        //values.put(CalendarContract.Events.SYNC_DATA1, "H-BRS TIMETABLE");
        values.put(CalendarContract.Events.RRULE, entry.rrule);
        values.put(CalendarContract.Events.EVENT_LOCATION, entry.location);


        //for(String exdate : entry.exdate)
        //  values.put(CalendarContract.Events.EXDATE, exdate);

        values.put(CalendarContract.Events.EXDATE, StringUtils.join(entry.exdate, '\n'));

        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        long eventID = Long.parseLong(uri.getLastPathSegment());


        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String eventIds = mPrefs.getString(CAL_EVENT_IDS, "");

        eventIds += eventID + "\n";

        mPrefs.edit().putString(CAL_EVENT_IDS, eventIds).commit();
    }
}
