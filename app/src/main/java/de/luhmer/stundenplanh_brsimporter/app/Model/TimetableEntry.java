package de.luhmer.stundenplanh_brsimporter.app.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by David on 01.04.2014.
 */
public class TimetableEntry implements Serializable, Comparable<TimetableEntry> {

    public static final long MILLIS_PER_DAY = 86400000;

    public String categories;
    public String description;
    public List<String> exdate = new ArrayList<String>();
    public long dtstart;
    public long dtend;

    public String location;
    public String rrule;
    public String summary;

    public TimetableEntry() {

    }

    @Override
    public int compareTo(TimetableEntry timetableEntry) {
        if (dtstart % MILLIS_PER_DAY < timetableEntry.dtstart % MILLIS_PER_DAY)
            return -1;
        else if (dtstart  % MILLIS_PER_DAY== timetableEntry.dtstart % MILLIS_PER_DAY)
            return 0;
        else
            return 1;
    }

    /*
    public TimetableEntry(String categories, String description, String exdate, String dtstart, String dtend, String location, String rrule, String summary) {
        this.categories = categories;
        this.summary = summary;
        this.description = description;
        this.exdate = exdate;
        this.dtstart = dtstart;
        this.dtend = dtend;
        this.location = location;
        this.rrule = rrule;
    }
    */
}