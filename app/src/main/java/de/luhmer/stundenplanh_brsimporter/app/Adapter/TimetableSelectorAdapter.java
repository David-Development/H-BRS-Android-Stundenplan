package de.luhmer.stundenplanh_brsimporter.app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.luhmer.stundenplanh_brsimporter.app.Model.TimetableSelectorEntry;
import de.luhmer.stundenplanh_brsimporter.app.R;

/**
 * Created by david on 03.04.14.
 */
public class TimetableSelectorAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "TimetableSelectorAdapter";
    // declaring our ArrayList of items
    private List<TimetableSelectorEntry> items;
    LayoutInflater inflater;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public TimetableSelectorAdapter(Context context, List<TimetableSelectorEntry> items) {
        this.items = items;
        inflater = ((Activity) context).getLayoutInflater();
    }


    @Override
    public int getGroupCount() {
        return items.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return items.get(i).subjects.size();
    }

    @Override
    public TimetableSelectorEntry getGroup(int i) {
        return items.get(i);
    }

    @Override
    public String getChild(int i, int i2) {
        return items.get(i).subjects.get(items.get(i).subjects.keyAt(i2));
    }

    public int getKeyAt(int i, int i2) {
        return items.get(i).subjects.keyAt(i2);
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i2) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int position, boolean b, View view, ViewGroup viewGroup) {
        // assign the view we are converting to a local variable
        ViewHolderItem viewHolder;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if(view == null){
            view = inflater.inflate(R.layout.timetable_selector_list_item, viewGroup, false);

            // well set up the ViewHolder
            viewHolder = new ViewHolderItem();
            viewHolder.tv_description = (TextView) view.findViewById(R.id.tv_description);
            viewHolder.ll_listItem = (LinearLayout) view.findViewById(R.id.ll_listItem);


            // store the holder with the view.
            view.setTag(viewHolder);

        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) view.getTag();
        }

        // object item based on the position
        TimetableSelectorEntry entry = getGroup(position);

        // assign values if the object is not null
        if(entry != null) {
            // get the TextView from the ViewHolder and then set the text (item name) and tag (item ID) values

            String description = entry.title.trim();
            viewHolder.tv_description.setText(description);
        }

        return view;
    }

    @Override
    public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
        // assign the view we are converting to a local variable
        ViewHolderItem viewHolder;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if(view == null){
            view = inflater.inflate(R.layout.timetable_selector_subject_list_item, viewGroup, false);

            // well set up the ViewHolder
            viewHolder = new ViewHolderItem();
            viewHolder.tv_description = (TextView) view.findViewById(R.id.tv_description);
            viewHolder.ll_listItem = (LinearLayout) view.findViewById(R.id.ll_listItem);


            // store the holder with the view.
            view.setTag(viewHolder);

        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) view.getTag();
        }

        // object item based on the position
        String entry = getChild(i, i2);
        viewHolder.tv_description.setText(entry);

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }


    // our ViewHolder.
    // caches our TextView
    static class ViewHolderItem {
        LinearLayout ll_listItem;
        TextView tv_description;
    }

}
