package de.luhmer.stundenplanh_brsimporter.app.Model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FB02ScheduleEntry {

    public Date startDate;
    public Date endDate;
    public String description;
    public String original_time;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    public FB02ScheduleEntry(String startDateString, String endDateString, String description, String original_time) {
        try {
            if(!startDateString.isEmpty()) {
                this.startDate = sdf.parse(startDateString);
            }
            this.endDate = sdf.parse(endDateString);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.description = description;
        this.original_time = original_time;
    }

    public FB02ScheduleEntry(Date startDate, Date endDate, String description, String original_time) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.original_time = original_time;
    }

}
