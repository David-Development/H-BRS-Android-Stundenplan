package de.luhmer.stundenplanh_brsimporter.app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

import de.luhmer.stundenplanh_brsimporter.app.Helper.Constants;
import de.luhmer.stundenplanh_brsimporter.app.Model.SubjectGroupEntry;
import de.luhmer.stundenplanh_brsimporter.app.R;
import de.luhmer.stundenplanh_brsimporter.app.TimetableImporterActivity;

/**
 * Created by david on 03.04.14.
 */
public class TimetableSelectGroupAdapter extends ArrayAdapter<SubjectGroupEntry> {
    private static final String TAG = "TimetableSelectGroupAdapter";
    // declaring our ArrayList of items
    private List<SubjectGroupEntry> items;
    LayoutInflater inflater;
    int idSubject;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public TimetableSelectGroupAdapter(Context context,  int textViewResourceId, List<SubjectGroupEntry> items, int idSubject) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.idSubject = idSubject;
        inflater = ((Activity) context).getLayoutInflater();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // assign the view we are converting to a local variable
        ViewHolderItem viewHolder;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if(view == null){
            view = inflater.inflate(R.layout.timetable_selector_group_list_item, viewGroup, false);

            // well set up the ViewHolder
            viewHolder = new ViewHolderItem();
            viewHolder.tv_description = (TextView) view.findViewById(R.id.tv_description);
            viewHolder.ll_listItem = (LinearLayout) view.findViewById(R.id.ll_listItem);
            viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkbox);

            // store the holder with the view.
            view.setTag(viewHolder);

        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) view.getTag();
        }

        // object item based on the position
        SubjectGroupEntry entry = getItem(i);

        String description = entry.title.trim();
        if(description.equals("-1")) {
            description = "Alle";
        }
        viewHolder.tv_description.setText(description);
        viewHolder.checkBox.setChecked(entry.selected);

        viewHolder.checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
        viewHolder.checkBox.setTag(i);

        return view;
    }


    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            Integer position = (Integer) compoundButton.getTag();
            if(position != null) {
                String idGroup = getItem(position).id;
                new ChangeMembershipOfGroupAsyncTask(idGroup, b).execute();
            }
        }
    };

    // our ViewHolder.
    // caches our TextView
    static class ViewHolderItem {
        LinearLayout ll_listItem;
        TextView tv_description;
        CheckBox checkBox;
    }


    private class ChangeMembershipOfGroupAsyncTask extends AsyncTask<Void, Void, Boolean> {

        String idGroup;
        Boolean selected;

        public ChangeMembershipOfGroupAsyncTask(String idGroup, Boolean selected) {
            this.idGroup = idGroup;
            this.selected = selected;
        }

        @Override
        protected Boolean doInBackground(Void... urls) {
            try {
                //Download list of semesters
                String result;
                if(selected)
                    result = TimetableImporterActivity.getBodyFromPageGet(Constants.API_URL + "?action=register_for_timetable_entry&idgroup=" + idGroup + "&idsubject=" + idSubject, getContext());
                else
                    result = TimetableImporterActivity.getBodyFromPageGet(Constants.API_URL + "?action=unregister_for_timetable_entry&idgroup=" + idGroup + "&idsubject=" + idSubject, getContext());
                JSONObject jsonResult = new JSONObject(result);
                return jsonResult.getBoolean("data");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return false;
        }


        @Override
        protected void onPostExecute(Boolean result) {

        }
    }
}
