package de.luhmer.stundenplanh_brsimporter.app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.stundenplanh_brsimporter.app.Model.FB02ScheduleEntry;
import de.luhmer.stundenplanh_brsimporter.app.R;

/**
 * Created by David on 05.07.2014.
 */
public class FB02ScheduleArrayAdapter extends ArrayAdapter<FB02ScheduleEntry> {
    private final String TAG = getClass().getCanonicalName();

    Integer paintTvDate;
    Integer paintTvDesc;

    LayoutInflater inflater;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<FB02ScheduleEntry> objects,
    * because it is the list of objects we want to display.
    */
    public FB02ScheduleArrayAdapter(Context context, int textViewResourceId, List<FB02ScheduleEntry> items) {
        super(context, textViewResourceId, items);
        inflater = ((Activity) getContext()).getLayoutInflater();
    }

    /*
     * we are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fb02_schedule_list_item, parent, false);
            viewHolder = new ViewHolderItem(convertView);

            paintTvDate = viewHolder.tvDate.getPaintFlags();
            paintTvDesc = viewHolder.tvDesc.getPaintFlags();

            convertView.setTag(viewHolder);

        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        // object item based on the position
        FB02ScheduleEntry entry = getItem(position);

        // assign values if the object is not null
        if (entry != null) {
            String dateText = "";
            if(entry.startDate != null) {
                dateText += DateFormatUtils.format(entry.startDate, "dd.MM.yyyy") + " - ";
            }
            dateText += DateFormatUtils.format(entry.endDate, "dd.MM.yyyy");

            viewHolder.tvDate.setText(dateText);
            viewHolder.tvDesc.setText(entry.description);

            if(entry.endDate.before(new Date())) {
                viewHolder.tvDate.setPaintFlags(paintTvDate | Paint.STRIKE_THRU_TEXT_FLAG);
                viewHolder.tvDesc.setPaintFlags(paintTvDesc | Paint.STRIKE_THRU_TEXT_FLAG);
                viewHolder.tvDesc.setTypeface(null, Typeface.NORMAL);
            } else {
                viewHolder.tvDate.setPaintFlags(paintTvDate);
                viewHolder.tvDesc.setPaintFlags(paintTvDesc);
                viewHolder.tvDesc.setTypeface(null, Typeface.BOLD);
            }
        }

        return convertView;
    }

    // our ViewHolder.
    static class ViewHolderItem {
        @InjectView(R.id.tv_date) TextView tvDate;
        @InjectView(R.id.tv_description) TextView tvDesc;

        public ViewHolderItem(View view) {
            ButterKnife.inject(this, view);
        }

    }
}