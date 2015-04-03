package de.luhmer.stundenplanh_brsimporter.app.timetable;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by David on 30.03.2015.
 */
public class WinterSummerTimeTests extends TimetableDateTests {

    public WinterSummerTimeTests() {
        event = "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "PRODID:https://play.google.com/store/apps/details?id=de.luhmer.stundenplanh_brsimporter.app\n" +
                "X-WR-CALDESC:HBRS Timetable Parser\n" +
                "X-WR-TIMEZONE:Europe/Berlin\n" +
                "BEGIN:VEVENT\n" +
                "UID:20150329T001707CET-2759JTaV5T@kigkonsult.se\n" +
                "DTSTAMP:20150328T231707Z\n" +
                "CATEGORIES:Ü\n" +
                "DESCRIPTION:Prassler\n" +
                "DTSTART;TZID=Europe/Berlin:20150328T151500\n" + //Starts in winter time
                "DTEND;TZID=Europe/Berlin:20150328T164500\n" +
                "LOCATION:St-C125\n" +
                "RRULE:FREQ=WEEKLY;UNTIL=20150630T164500Z;INTERVAL=1\n" +
                "SUMMARY:Probabilistic  Methods for Robotics\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR";
    }

    //Test Event in Summer Time
    public void testEventFromWithWinterTimeInSummerTime() throws ParseException {
        String dateInString = "04.04.2015 10:00:00";
        Date date = sdf.parse(dateInString);
        parseCalendar(event, date);
        assertEquals("15:15 - 16:45" , GetTimeString(timetableEntries.get(0), date));
    }


    //Test Event in Winter Time
    public void testEventFromWithWinterTimeInWinterTime() throws ParseException {
        String dateInString = "28.03.2015 10:00:00";
        Date date = sdf.parse(dateInString);
        parseCalendar(event, date);
        assertEquals("15:15 - 16:45" , GetTimeString(timetableEntries.get(0), date));
    }

}
