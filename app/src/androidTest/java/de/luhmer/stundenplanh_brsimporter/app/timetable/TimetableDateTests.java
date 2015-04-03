package de.luhmer.stundenplanh_brsimporter.app.timetable;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import junit.framework.TestCase;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.util.CompatibilityHints;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.luhmer.stundenplanh_brsimporter.app.Model.TimetableEntry;
import de.luhmer.stundenplanh_brsimporter.app.TimetableImporterActivity;

/**
 * Created by David on 30.03.2015.
 */
public class TimetableDateTests extends AndroidTestCase {

    private final String TAG = getClass().getCanonicalName();
    List<TimetableEntry> timetableEntries;
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

    String event;



    @Override
    protected void setUp() throws Exception {
        timetableEntries = new ArrayList<>();

        Thread.currentThread().setContextClassLoader(getContext().getClassLoader());
        CompatibilityHints.setHintEnabled("ical4j.unfolding.relaxed", true);

        super.setUp();
    }


    protected void parseCalendar(String calendarString, Date date) {
        try {
            Period period = new Period(new DateTime(date), new Dur(1, 0, 0, 0));
            Filter filter = new Filter(new PeriodRule(period));

            UnfoldingReader reader = new UnfoldingReader(new StringReader(calendarString), 5000);
            Calendar calendar = new CalendarBuilder().build(reader);

            List eventsFiltered;
            if (filter != null) {
                eventsFiltered = (List) filter.filter(calendar.getComponents(Component.VEVENT));
            } else {
                eventsFiltered = calendar.getComponents(Component.VEVENT);
            }

            // For each VEVENT in the ICS
            for (Object o : eventsFiltered) {
                Component c = (Component) o;
                timetableEntries.add(TimetableImporterActivity.getTimetableEntryByVEvent(c));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    protected static String GetTimeString(TimetableEntry entry, Date date) {
        long startTime = entry.dtstart % AlarmManager.INTERVAL_DAY;
        long endTime = entry.dtend % AlarmManager.INTERVAL_DAY;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        Date dtStart = new Date(cal.getTimeInMillis() + startTime);
        Date dtEnd = new Date(cal.getTimeInMillis() + endTime);


        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String dateStart = dateFormat.format(dtStart.getTime());
        String dateEnd = dateFormat.format(dtEnd.getTime());
        String timeString = dateStart + " - " + dateEnd;
        return timeString;
    }

}
