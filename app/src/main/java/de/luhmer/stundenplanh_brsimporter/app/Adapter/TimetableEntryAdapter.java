package de.luhmer.stundenplanh_brsimporter.app.Adapter;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.luhmer.stundenplanh_brsimporter.app.Model.TimetableEntry;
import de.luhmer.stundenplanh_brsimporter.app.R;

/**
 * Created by david on 03.04.14.
 */
public class TimetableEntryAdapter extends ArrayAdapter<TimetableEntry> {
    private static final String TAG = "TimetableEntryAdapter";
    // declaring our ArrayList of items
    private List<TimetableEntry> items;
    TimeZone timeZone;
    Date date;
    LayoutInflater inflater;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public TimetableEntryAdapter(Context context, int textViewResourceId, List<TimetableEntry> items, Date date) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.date = date;

        Calendar cal = Calendar.getInstance();
        timeZone = cal.getTimeZone();

        inflater = ((Activity) getContext()).getLayoutInflater();
    }

    /*
    public List<TimetableEntry> getItems() {
        return items;
    }
    */

    /*
     * we are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    public View getView(int position, View convertView, ViewGroup parent){

        // assign the view we are converting to a local variable
        ViewHolderItem viewHolder;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if(convertView==null){
            convertView = inflater.inflate(R.layout.timetable_list_item, parent, false);

            // well set up the ViewHolder
            viewHolder = new ViewHolderItem();
            viewHolder.tv_description = (TextView) convertView.findViewById(R.id.tv_description);
            viewHolder.tv_room = (TextView) convertView.findViewById(R.id.tv_room);
            viewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
            viewHolder.img_type = (ImageView) convertView.findViewById(R.id.img_type);
            viewHolder.ll_listItem = (LinearLayout) convertView.findViewById(R.id.ll_listItem);


            // store the holder with the view.
            convertView.setTag(viewHolder);

        }else{
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        // object item based on the position
        TimetableEntry entry = items.get(position);

        // assign values if the object is not null
        if(entry != null) {
            // get the TextView from the ViewHolder and then set the text (item name) and tag (item ID) values

            //TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            String description = entry.description.trim();
            description = description.replace("Dozenten: ", "");
            description = description.replace("\n", " - ");


            if(entry.categories.equals("V"))
                viewHolder.tv_type.setText("Vorlesung");
            else if(entry.categories.equals("Ü"))
                viewHolder.tv_type.setText("Übung");
            else
                viewHolder.tv_type.setText(entry.categories);


            viewHolder.tv_room.setText(entry.location + " - " + description.trim());
            viewHolder.tv_description.setText(entry.summary);


            //Calendar calStart = Calendar.getInstance();
            //calStart.setTimeInMillis(entry.dtstart);

            //long startHour = entry.dtstart % AlarmManager.INTERVAL_DAY);
            //long startMinute = ((entry.dtstart % AlarmManager.INTERVAL_DAY) % AlarmManager.INTERVAL_HOUR);

            long startTime = entry.dtstart % AlarmManager.INTERVAL_DAY;
            long endTime = entry.dtend % AlarmManager.INTERVAL_DAY;


            //calStart.setTimeInMillis(entry.dtstart);

            Date dtStart = new Date(entry.dtstart);
            /*
            int offset = dtStart.getTimezoneOffset() - new Date().getTimezoneOffset();

            if(offset == 0) {
                Date lastMarch = new Date(1396238400000L);
                int diffInDays = TimetableActivity.getDiffInDaysOfDates(lastMarch, dtStart);

                if(diffInDays != 0) {
                    startTime += AlarmManager.INTERVAL_HOUR;
                    endTime += AlarmManager.INTERVAL_HOUR;

                    Log.d(TAG, "Summary: " + entry.summary + " start date: " + entry.dtstart + " offset: " + offset);
                } else {
                    Log.d(TAG, "TimeZone Exception!!!! - Summary: " + entry.summary + " start date: " + entry.dtstart + " offset: " + offset);
                }
            }
            */






            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 1);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            dtStart = new Date(cal.getTimeInMillis() + startTime);
            Date dtEnd = new Date(cal.getTimeInMillis() + endTime);

            //Log.d(TAG, "\nTimezone: " + calStart.getTimeZone().getDisplayName() + "\nCal Time: " + cal.getTime().toString() + "\nDate Start: " + dtStart.toString() + "\nDate End: " + dtEnd.toString());


            DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            String dateStart = dateFormat.format(dtStart.getTime());
            String dateEnd = dateFormat.format(dtEnd.getTime());
            String timeString = dateStart + " - " + dateEnd;
            viewHolder.tv_time.setText(timeString);



            int colorId = R.color.timetable_background_item_default;
            int diffInDays = 0; //TimetableActivity.getDiffInDaysOfDates(new Date(), dtStart);//TODO THIS LINE IS NEEDED!!!
            if(diffInDays == 0) {
                long cDate = new Date().getTime();

                if(cDate > dtEnd.getTime())
                    colorId = R.color.timetable_background_item_passed;
                else if(cDate > dtStart.getTime() && cDate < dtEnd.getTime())
                    colorId = R.color.timetable_background_item_active;
            }

            viewHolder.ll_listItem.setBackgroundColor(getContext().getResources().getColor(colorId));


            if(entry.categories != null && entry.categories.equals("Ü"))
                viewHolder.img_type.setImageResource(R.drawable.uebung);
            else if(entry.categories != null && entry.categories.equals("V"))
                viewHolder.img_type.setImageResource(R.drawable.vorlesung);
            else
                viewHolder.img_type.setImageResource(R.drawable.other);

        }

        return convertView;
    }

    public Date getNormalizedDate(Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        cal.add(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        cal.add(Calendar.MINUTE, calendar.get(Calendar.MINUTE));

        return cal.getTime();
    }


    // our ViewHolder.
    // caches our TextView
    static class ViewHolderItem {
        LinearLayout ll_listItem;
        ImageView img_type;
        TextView tv_type;
        TextView tv_time;
        TextView tv_description;
        TextView tv_room;

    }

}
