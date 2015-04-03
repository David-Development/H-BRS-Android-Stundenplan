package de.luhmer.stundenplanh_brsimporter.app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang.time.DateFormatUtils;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamItem;
import de.luhmer.stundenplanh_brsimporter.app.R;

/**
 * Created by David on 05.07.2014.
 */
public class ExamArrayAdapter extends ArrayAdapter<ExamItem> {
    private static final String TAG = "ExamArrayAdapter";
    LayoutInflater inflater;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public ExamArrayAdapter(Context context, int textViewResourceId, List<ExamItem> items) {
        super(context, textViewResourceId, items);
        inflater = ((Activity) getContext()).getLayoutInflater();
    }


    /*
     * we are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.exam_list_item, parent, false);
            viewHolder = new ViewHolderItem(convertView);
            convertView.setTag(viewHolder);

        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        // object item based on the position
        ExamItem entry = getItem(position);

        // assign values if the object is not null
        if (entry != null) {
            viewHolder.tvDate.setText(DateFormatUtils.format(entry.terminKlausur, "dd.MM.yyyy"));
            viewHolder.tvDescTop.setText(entry.examId + " - Versuch: " + entry.versuch);
            viewHolder.tvDesc.setText(entry.fachName);
            viewHolder.tvDescBottom.setText("Note: " + entry.note + " - " + entry.credits + "CP");
            //viewHolder.ll_listItem.setBackgroundColor(getContext().getResources().getColor(colorId));

            viewHolder.img_type.setImageResource(getImageResourceForExamStatus(entry));
        }

        return convertView;
    }

    public static int getImageResourceForExamStatus(ExamItem examItem) {
        if(examItem.status.equals("BE")) {
            return R.drawable.exam_success;
        } else if(examItem.status.equals("NB")) {
            return R.drawable.exam_failed;
        } else {
            return R.drawable.exam_pending;
        }
    }


    // our ViewHolder.
    // caches our TextView
    static class ViewHolderItem {
        @InjectView(R.id.ll_listItem) LinearLayout ll_listItem;
        @InjectView(R.id.img_type) ImageView img_type;
        @InjectView(R.id.tv_date) TextView tvDate;
        @InjectView(R.id.tv_desc_top) TextView tvDescTop;
        @InjectView(R.id.tv_desc_bottom) TextView tvDescBottom;
        @InjectView(R.id.tv_description) TextView tvDesc;

        public ViewHolderItem(View view) {
            ButterKnife.inject(this, view);
        }

    }
}